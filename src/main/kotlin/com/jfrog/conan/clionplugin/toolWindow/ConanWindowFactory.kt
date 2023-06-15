package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.collaboration.ui.selectFirst
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBSplitter
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.text.SemVer
import com.intellij.util.ui.JBUI
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.conan.Conan
import com.jfrog.conan.clionplugin.conan.ConanPluginUtils
import com.jfrog.conan.clionplugin.conan.datamodels.Recipe
import com.jfrog.conan.clionplugin.dialogs.ConanExecutableDialogWrapper
import com.jfrog.conan.clionplugin.dialogs.ConanInspectPackagesDialogWrapper
import com.jfrog.conan.clionplugin.dialogs.ConanInstallDialogWrapper
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
import com.jfrog.conan.clionplugin.services.ConanService
import com.jfrog.conan.clionplugin.services.RemotesDataStateService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableRowSorter


class ConanWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    private val contentFactory = ContentFactory.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = ConanWindow(toolWindow, project)
        val content = contentFactory.createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class ConanWindow(toolWindow: ToolWindow, project: Project) {
        private val project = project
        private val stateService = this.project.service<RemotesDataStateService>()

        fun getContent() = OnePixelSplitter(false).apply {

            val secondComponentPanel = JBPanelWithEmptyText().apply {
                layout = BorderLayout()
                border = JBUI.Borders.empty(10)
            }

            firstComponent = DialogPanel(BorderLayout()).apply {
                border = JBUI.Borders.empty(5)
                val searchTextField = SearchTextField()

                val actionGroup = DefaultActionGroup().apply {
                    add(object : AnAction("Configure Conan", null, AllIcons.General.Settings) {
                        override fun actionPerformed(e: AnActionEvent) {
                            ConanExecutableDialogWrapper(project).showAndGet()
                        }
                    })
                    add(object : AnAction("Add Conan support to Project", null, AllIcons.General.Add) {
                        // TODO: probably add for all configurations by default would be better
                        override fun actionPerformed(e: AnActionEvent) {
                            val dialog = ConanInstallDialogWrapper(project)
                            // FIXME: duplicated code from runUseFlow
                            if (dialog.showAndGet()) {
                                val conanExecutable: String = project.service<PropertiesComponent>().getValue(
                                        PersistentStorageKeys.CONAN_EXECUTABLE,
                                        "conan"
                                )
                                dialog.getSelectedInstallProfiles().forEach { profileName ->
                                    thisLogger().info("Adding Conan configuration to $profileName")
                                    CMake(project).addGenerationOptions(profileName,
                                            listOf("-DCMAKE_PROJECT_TOP_LEVEL_INCLUDES=\"${ConanPluginUtils.getCmakeProviderPath()}\"",
                                                    "-DCONAN_COMMAND=\"${conanExecutable}\"")
                                    )
                                }
                            }
                        }
                    })
                    add(object : AnAction("Update packages", null, AllIcons.Actions.Refresh) {
                        override fun actionPerformed(e: AnActionEvent) {

                            ConanPluginUtils.downloadCMakeProvider(true)

                            Conan(project).list("*") { runOutput ->
                                if (runOutput.exitCode == 0) {
                                    val newJson = Json.decodeFromString<RemotesDataStateService.State>(runOutput.stdout)
                                    stateService.loadState(newJson)
                                    NotificationGroupManager.getInstance()
                                            .getNotificationGroup("Conan Notifications Group")
                                            .createNotification("Updated remote data",
                                                    "Remote data has been updated",
                                                    NotificationType.INFORMATION)
                                            .notify(project);
                                } else {
                                    NotificationGroupManager.getInstance()
                                            .getNotificationGroup("Conan Notifications Group")
                                            .createNotification("Error updating remote data",
                                                    "Conan returned non 0 exit for the installation",
                                                    NotificationType.ERROR)
                                            .notify(project);
                                }
                            }
                        }
                    })
                    add(object : AnAction("Check used packages", null, AllIcons.General.InspectionsEye) {
                        override fun actionPerformed(e: AnActionEvent) {
                            ConanInspectPackagesDialogWrapper(project).showAndGet()
                        }
                    })
                }
                val actionToolbar = ActionManager.getInstance().createActionToolbar("ConanToolbar", actionGroup, true)
                actionToolbar.targetComponent = this

                var recipes: List<Recipe> = listOf()
                val columnNames = arrayOf("Name")
                val dataModel = object : DefaultTableModel(columnNames, 0) {
                    // By default cells are editable and that's no good. Override the function that tells the UI it is
                    // TODO: Find the proper configuration for this, this can't be the proper way to make it static
                    override fun isCellEditable(row: Int, column: Int): Boolean {
                        return false
                    }
                }
                val versionModel = DefaultComboBoxModel<String>()

                stateService.addStateChangeListener(object : RemotesDataStateService.RemoteDataStateListener {
                    override fun stateChanged(newState: RemotesDataStateService.State?) {
                        dataModel.rowCount = 0
                        recipes = listOf()

                        if (newState == null) return

                        // conancenter has one entry per recipe version, this collates all versions into 1 recipe object,
                        // with a versions list of each of the existing ones
                        recipes = newState.conancenter.keys
                            .map {
                                val split = it.split("/")
                                Pair(split[0], split[1])
                            }
                            .groupBy { it.first }
                            .map {
                                dataModel.addRow(arrayOf(it.key))
                                Recipe(it.key, it.value.map{ it.second })
                            }
                    }
                }
                )

                val packagesTable = JBTable(dataModel).apply {
                    tableHeader
                    autoCreateRowSorter = true
                    (rowSorter as TableRowSorter<DefaultTableModel>).sortKeys =
                        mutableListOf(RowSorter.SortKey(0, SortOrder.ASCENDING))
                }


                searchTextField.apply {
                    addDocumentListener(object : DocumentAdapter() {
                        override fun textChanged(e: DocumentEvent) {
                            (packagesTable.rowSorter as TableRowSorter<DefaultTableModel>).rowFilter = RowFilter.regexFilter(".*$text.*")
                        }
                    })
                }

                packagesTable.selectionModel.addListSelectionListener {
                    val selectedRow = packagesTable.selectedRow
                    if (selectedRow != -1) {
                        val name = packagesTable.getValueAt(selectedRow, 0) as String

                        val recipe = recipes.find { it.name == name } ?: throw Exception()
                        val versions = recipe.versions.sortedByDescending(SemVer::parseFromText)
                        versionModel.apply {
                            removeAllElements()
                            addAll(versions)
                            selectFirst()
                        }

                        secondComponentPanel.removeAll()
                        secondComponentPanel.add(JLabel(name).apply {
                            font = font.deriveFont(Font.BOLD, 18f) // set font size to 18 and bold
                        }, BorderLayout.NORTH)

                        secondComponentPanel.add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                            val comboBox = ComboBox(versionModel)

                            add(comboBox)
                            add(JButton("Use in project").apply {
                                addActionListener {
                                    project.service<ConanService>().runUseFlow(name, comboBox.selectedItem as String)
                                }
                            })
                        })

                        secondComponentPanel.revalidate()
                        secondComponentPanel.repaint()
                    }
                }

                add(JBSplitter().apply {
                    firstComponent = searchTextField
                    secondComponent = JPanel(BorderLayout()).apply {
                        add(actionToolbar.component, BorderLayout.EAST)
                    }
                }, BorderLayout.PAGE_START)
                add(JBScrollPane(packagesTable), BorderLayout.CENTER)
            }
            secondComponent = secondComponentPanel.apply { withEmptyText("No selection") }
            proportion = 0.2f
        }

    }
}
