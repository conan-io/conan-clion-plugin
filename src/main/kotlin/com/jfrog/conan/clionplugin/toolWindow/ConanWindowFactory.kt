package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.jfrog.conan.clionplugin.services.MyProjectService
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.table.DefaultTableModel


class ConanWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    private val contentFactory = ContentFactory.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = ConanWindow(toolWindow)
        val content = contentFactory.createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class ConanWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()
        fun getContent() = OnePixelSplitter(false).apply {
            firstComponent = DialogPanel(BorderLayout()).apply {
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

                val dataModel = DefaultTableModel(arrayOf("Column 1", "Column 2", "Column 3"), 0)
                val exampleTable = JBTable(dataModel)

                val boxLayout = Box.createVerticalBox()
                boxLayout.add(actionToolbar.component)

                val scrollablePane = JBScrollPane(exampleTable)

                boxLayout.add(scrollablePane)

                this.add(boxLayout, BorderLayout.WEST)
            }

            secondComponent = JBPanelWithEmptyText().apply { withEmptyText("No selection") }
        }

    }
}
