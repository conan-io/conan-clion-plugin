package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.ui.addKeyboardAction
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.jfrog.conan.clionplugin.models.ListResult
import com.jfrog.conan.clionplugin.services.MyProjectService
import org.jetbrains.debugger.filterAndSort
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel


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

        private val service = toolWindow.project.service<MyProjectService>()
        private val project = project
        private val recipes = ListResult()

//        fun getContent() = JBPanel<JBPanel<*>>().apply {
//
//            val descriptionPanel = JBPanelWithEmptyText().apply { withEmptyText("Select a package to show its information.") }
//            val searchTextField = SearchTextField()
//
//
//            add(OnePixelSplitter(false).apply {
//                firstComponent = DialogPanel().apply {
//                    add(searchTextField.apply {
//
//                    })
//                    add(JButton("Search").apply {
//                        addActionListener {
//                            // Do nothing for now
//                            val searchText = searchTextField.text
//                            thisLogger().info("Search button clicked. Text: $searchText")
//                            Messages.showMessageDialog(
//                                    project,
//                                    "---dd---",
//                                    searchText,
//                                    Messages.getInformationIcon()
//                            )
//                        }
//                    })
//
//                    add(JBTable(recipes).apply {
//                        service.refreshListListeners.add { it -> recipes.updateList(
//                            it.conancenter.map {
//                                val (name, version) = it.key.split("/")
//                                ListResult.ListResultRow(name, version, "$name is a library currently in version $version")
//                            }
//                        )}
//
//                        selectionModel.apply {
//                            selectionMode = ListSelectionModel.SINGLE_SELECTION
//                            addListSelectionListener {
//                                descriptionPanel.removeAll()
//                                descriptionPanel.add(JBLabel(recipes.getRecipeAtRow(selectedRow).description))
//                            }
//                        }
//                    })
//                }
//
//                secondComponent = descriptionPanel
//            })
//        }
        fun getContent() = OnePixelSplitter(false).apply {
            firstComponent = DialogPanel(FlowLayout()).apply {
                val actionGroup = DefaultActionGroup().apply {
                    add(object : AnAction("Button 1") {
                        override fun actionPerformed(e: AnActionEvent) {
                            // do something on button 1 click
                        }
                    })
                    add(object : AnAction("Button 2") {
                        override fun actionPerformed(e: AnActionEvent) {
                            // do something on button 2 click
                        }
                    })
                    add(object : AnAction("Button 3") {
                        override fun actionPerformed(e: AnActionEvent) {
                            // do something on button 3 click
                        }
                    })
                    add(object : AnAction("Button 4") {
                        override fun actionPerformed(e: AnActionEvent) {
                            // do something on button 4 click
                        }
                    })
                }

                val actionToolbar = ActionManager.getInstance().createActionToolbar("MyToolbar", actionGroup, true)
                add(actionToolbar.component)

                val dataModel = DefaultTableModel(arrayOf("Column 1", "Column 2", "Column 3"), 0)
                val exampleTable = JBTable(dataModel)
                add(JBScrollPane(exampleTable))
            }

            secondComponent = JBPanelWithEmptyText().apply { withEmptyText("No selection") }
        }
    }
}

//val Project.conanWindow: ToolWindow?
//    get() = ToolWindowManager.getInstance(this).getToolWindow("Conan")