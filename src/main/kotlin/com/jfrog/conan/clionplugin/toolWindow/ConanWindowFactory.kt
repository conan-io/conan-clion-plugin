package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.addKeyboardAction
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
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
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.ListSelectionModel


class ConanWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    private val contentFactory = ContentFactory.SERVICE.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = ConanWindow(toolWindow)
        val content = contentFactory.createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class ConanWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()

        private val recipes = ListResult()
        fun getContent() = JBPanel<JBPanel<*>>().apply {

            val descriptionPanel = JBPanelWithEmptyText().apply { withEmptyText("Select a package to show its information.") }

            add(OnePixelSplitter(false).apply {
                firstComponent = DialogPanel().apply {
                    val searchField = SearchTextField()
                    add(searchField)
                    add(JButton("Search").apply {
                        addActionListener {
                            recipes.updateList(service.getConanPackages(searchField.text).conancenter.map {
                                val (name, version) = it.key.split("/")
                                ListResult.ListResultRow(name, version, "$name is a library currently in version $version")
                            })
                        }
                    })

                    add(JBTable(recipes).apply {
                        selectionModel.apply {
                            selectionMode = ListSelectionModel.SINGLE_SELECTION
                            addListSelectionListener {
                                descriptionPanel.removeAll()
                                descriptionPanel.add(JBLabel(recipes.getRecipeAtRow(selectedRow).description))
                            }
                        }
                    })
                }

                secondComponent = descriptionPanel
            })
        }
    }
}

