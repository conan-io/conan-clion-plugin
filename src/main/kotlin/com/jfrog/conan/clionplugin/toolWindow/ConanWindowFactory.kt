package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.collaboration.ui.selectFirst
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBSplitter
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JCEFHtmlPanel
import com.intellij.ui.table.JBTable
import com.intellij.util.text.SemVer
import com.intellij.util.ui.JBUI
import com.jfrog.conan.clionplugin.bundles.UIBundle
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.conan.Conan
import com.jfrog.conan.clionplugin.conan.datamodels.Recipe
import com.jfrog.conan.clionplugin.dialogs.ConanExecutableDialogWrapper
import com.jfrog.conan.clionplugin.dialogs.ConanInspectPackagesDialogWrapper
import com.jfrog.conan.clionplugin.services.ConanService
import com.jfrog.conan.clionplugin.services.RemotesDataStateService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableRowSorter


class ConanWindowFactory : ToolWindowFactory {

    private val contentFactory = ContentFactory.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = ConanWindow(toolWindow, project)
        val content = contentFactory.createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class ConanWindow(toolWindow: ToolWindow, val project: Project) {
        private val stateService = this.project.service<RemotesDataStateService>()
        private val conanService = project.service<ConanService>()

        fun getContent() = OnePixelSplitter(false).apply {

            val secondComponentPanel = JBPanelWithEmptyText()
            val htmlPanel = JCEFHtmlPanel(null)
            htmlPanel.loadHTML("")

            secondComponentPanel.layout = BoxLayout(secondComponentPanel, BoxLayout.Y_AXIS)

            firstComponent = DialogPanel(BorderLayout()).apply {
                border = JBUI.Borders.empty(5)
                val searchTextField = SearchTextField()

                val actionGroup = DefaultActionGroup().apply {
                    add(object : AnAction(
                        UIBundle.message("toolbar.action.show.dialog.configure"),
                        null,
                        AllIcons.General.Settings
                    ) {
                        override fun actionPerformed(e: AnActionEvent) {
                            ConanExecutableDialogWrapper(project).showAndGet()
                        }
                    })
                    add(object :
                        AnAction(UIBundle.message("toolbar.action.add.conan.support"), null, AllIcons.General.Add) {
                        override fun actionPerformed(e: AnActionEvent) {
                            val cmake = CMake(project)
                            cmake.addConanSupport()
                        }
                    })
                    add(object :
                        AnAction(UIBundle.message("toolbar.action.update"), null, AllIcons.Actions.Refresh) {
                        override fun actionPerformed(e: AnActionEvent) {

                            conanService.downloadCMakeProvider(true)

                            Conan(project).list("*") { runOutput ->
                                if (runOutput.exitCode == 0) {
                                    val newJson = Json.decodeFromString<RemotesDataStateService.State>(runOutput.stdout)
                                    stateService.loadState(newJson)
                                    NotificationGroupManager.getInstance()
                                        .getNotificationGroup("com.jfrog.conan.clionplugin.notifications.general")
                                        .createNotification(
                                            UIBundle.message("update.successful.title"),
                                            UIBundle.message("update.successful.body"),
                                            NotificationType.INFORMATION
                                        )
                                        .notify(project)
                                } else {
                                    NotificationGroupManager.getInstance()
                                        .getNotificationGroup("com.jfrog.conan.clionplugin.notifications.general")
                                        .createNotification(
                                            UIBundle.message("update.error.title"),
                                            UIBundle.message("update.error.body"),
                                            NotificationType.ERROR
                                        )
                                        .notify(project)
                                }
                            }
                        }
                    })
                    add(object : AnAction(
                        UIBundle.message("toolbar.action.show.used.packages"),
                        null,
                        AllIcons.General.InspectionsEye
                    ) {
                        override fun actionPerformed(e: AnActionEvent) {
                            ConanInspectPackagesDialogWrapper(project).showAndGet()
                        }
                    })
                }
                val actionToolbar = ActionManager.getInstance().createActionToolbar("ConanToolbar", actionGroup, true)
                actionToolbar.targetComponent = this

                var recipes: List<Recipe> = listOf()
                val columnNames = arrayOf(UIBundle.message("libraries.list.table.name"))
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
                                Recipe(it.key, it.value.map { it.second })
                            }
                    }
                })

                val packagesTable = JBTable(dataModel).apply {
                    tableHeader
                    autoCreateRowSorter = true
                    (rowSorter as TableRowSorter<DefaultTableModel>).sortKeys =
                        mutableListOf(RowSorter.SortKey(0, SortOrder.ASCENDING))
                }


                searchTextField.apply {
                    addDocumentListener(object : DocumentAdapter() {
                        override fun textChanged(e: DocumentEvent) {
                            (packagesTable.rowSorter as TableRowSorter<DefaultTableModel>).rowFilter =
                                RowFilter.regexFilter(".*$text.*")
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

                        secondComponentPanel.add(JBLabel(name).apply {
                            font = font.deriveFont(Font.BOLD, 18f)
                            alignmentX = Component.LEFT_ALIGNMENT
                        })

                        val buttonsPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                            alignmentX = Component.LEFT_ALIGNMENT

                            val comboBox = ComboBox(versionModel)

                            add(comboBox)


                            val addButton = JButton(UIBundle.message("library.description.button.install"))
                            val removeButton = JButton(UIBundle.message("library.description.button.remove"))

                            add(addButton)
                            add(removeButton)

                            addButton.addActionListener {
                                conanService.runUseFlow(name, comboBox.selectedItem as String)
                                val isRequired = conanService.getRequirements().any { it.startsWith("$name/") }
                                addButton.isVisible = !isRequired
                                removeButton.isVisible = isRequired
                                Messages.showMessageDialog(
                                    UIBundle.message("library.added.body", comboBox.selectedItem as String),
                                    UIBundle.message("library.added.title"),
                                    Messages.getInformationIcon()
                                )
                            }

                            removeButton.addActionListener {
                                conanService.runRemoveRequirementFlow(name, comboBox.selectedItem as String)
                                val isRequired = conanService.getRequirements().any { it.startsWith("$name/") }
                                addButton.isVisible = !isRequired
                                removeButton.isVisible = isRequired
                                Messages.showMessageDialog(
                                    UIBundle.message("library.removed.body", comboBox.selectedItem as String),
                                    UIBundle.message("library.removed.title"),
                                    Messages.getInformationIcon()
                                )
                            }

                            val isRequired = conanService.getRequirements().any { it.startsWith("$name/") }
                            addButton.isVisible = !isRequired
                            removeButton.isVisible = isRequired
                        }
                        secondComponentPanel.add(buttonsPanel)

                        val htmlContent = """
                                <html>
                                <body>
                                <h1>$name</h1>
                                <pre>    
                                "tsl-sparse-map": {
                                    "cmake_file_name": "tsl-sparse-map",
                                    "cmake_target_name": "tsl::sparse_map",
                                    "components": {
                                        "sparse_map": {
                                            "cmake_target_name": "tsl::sparse_map"
                                        }
                                    }
                                }
                                </pre>
                                </body>
                                </html>
                            """.trimIndent()

                        htmlPanel.loadHTML(htmlContent)

                        secondComponentPanel.add(htmlPanel.component)

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
            secondComponent =
                secondComponentPanel.apply { withEmptyText(UIBundle.message("library.description.empty")) }
            proportion = 0.2f
        }

    }
}
