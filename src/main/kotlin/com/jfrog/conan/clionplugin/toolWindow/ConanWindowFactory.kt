package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.observable.util.whenItemSelected
import com.intellij.openapi.observable.util.whenKeyReleased
import com.intellij.openapi.observable.util.whenKeyTyped
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
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.jfrog.conan.clionplugin.conan.Conan
import com.jfrog.conan.clionplugin.conan.datamodels.Recipe
import com.jfrog.conan.clionplugin.dialogs.ConanExecutableDialogWrapper
import com.jfrog.conan.clionplugin.services.RemotesDataStateService
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Font
import java.util.regex.Pattern
import javax.swing.*
import javax.swing.border.EmptyBorder
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
                    add(object : AnAction("Update packages", null, AllIcons.Actions.Refresh) {
                        override fun actionPerformed(e: AnActionEvent) {
                            Messages.showMessageDialog(
                                    project,
                                    "Update packages",
                                    "This will update Conan packages",
                                    Messages.getInformationIcon()
                            )
                        }
                    })
                }
                val actionToolbar = ActionManager.getInstance().createActionToolbar("ConanToolbar", actionGroup, true)

                var recipes: List<Recipe> = listOf()
                val columnNames = arrayOf("Name", "version")
                val dataModel = DefaultTableModel(columnNames, 0)
                val versionModel = DefaultComboBoxModel<String>()

                stateService.addStateChangeListener(object : RemotesDataStateService.RemoteDataStateListener {
                        override fun stateChanged(newState: RemotesDataStateService.State) {
                            recipes = newState.conancenter.keys.map {
                                val split = it.split("/")
                                Pair(split[0], split[1])
                            }.groupBy { it.first }.map { Recipe(it.key, it.value.map{ it.second }) }

                            dataModel.rowCount = 0
                            recipes.forEach { pkg ->
                                dataModel.addRow(arrayOf(pkg.name))
                            }
                        }
                    }
                )

                val packagesTable = JBTable(dataModel).apply {
                    tableHeader
                    autoCreateRowSorter = true
                    (rowSorter as TableRowSorter<DefaultTableModel>).apply {
                        sortKeys = mutableListOf(RowSorter.SortKey(0, SortOrder.ASCENDING))
                    }
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
                        val versions = recipe.versions
                        versionModel.removeAllElements()
                        versionModel.addAll(versions)

                        secondComponentPanel.removeAll()
                        secondComponentPanel.add(JLabel(name).apply {
                            font = font.deriveFont(Font.BOLD, 18f) // set font size to 18 and bold
                        }, BorderLayout.NORTH)

                        secondComponentPanel.add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                            val comboBox = ComboBox(versionModel)
                            val installButton = JButton("Install").apply {
                                isEnabled = false
                                addActionListener {
                                    val output = Conan(project).install(name, comboBox.selectedItem as String)
                                    NotificationGroupManager.getInstance()
                                        .getNotificationGroup("Conan Notifications Group")
                                        .createNotification("$name/${comboBox.selectedItem as String} installed successfully", "Conan output:\n$output", NotificationType.INFORMATION)
                                        .notify(project);
                                }
                            }
                            comboBox.apply {
                                whenItemSelected {
                                    installButton.isEnabled = true
                                }
                            }
                            add(installButton)
                            add(comboBox)
                        })

                        secondComponentPanel.revalidate()
                        secondComponentPanel.repaint()
                    }
                }

                val scrollablePane = JBScrollPane(packagesTable)

                add(JBSplitter().apply {
                    firstComponent = searchTextField
                    secondComponent = JPanel(BorderLayout()).apply {
                        add(actionToolbar.component, BorderLayout.EAST)
                    }
                }, BorderLayout.PAGE_START)
                add(scrollablePane, BorderLayout.CENTER)
            }
            secondComponent = secondComponentPanel.apply { withEmptyText("No selection") }
            proportion = 0.28f
        }

    }
}
