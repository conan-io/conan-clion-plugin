package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.jfrog.conan.clionplugin.services.MyProjectService
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel

data class Package(val name: String, val version: String)

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

        fun getContent() = OnePixelSplitter(false).apply {
            val secondComponentPanel = JBPanelWithEmptyText().apply {
                layout = BorderLayout()
                border = EmptyBorder(10, 10, 10, 10)
            }
            firstComponent = DialogPanel(BorderLayout()).apply {
                val actionGroup = DefaultActionGroup().apply {
                    add(object : AnAction("Configure Conan", null, AllIcons.General.Settings) {
                        override fun actionPerformed(e: AnActionEvent) {
                            Messages.showMessageDialog(
                                    project,
                                    "Configure Conan",
                                    "This will configure Conan",
                                    Messages.getInformationIcon()
                            )
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

                val packages = listOf(
                        Package("zlib", "1.2.13"),
                        Package("opencv", "4.5.5"),
                        // Add more packages here
                )

                val columnNames = arrayOf("Name", "Version")
                val dataModel = DefaultTableModel(columnNames, 0)

                packages.forEach { pkg ->
                    dataModel.addRow(arrayOf(pkg.name, pkg.version))
                }

                val packagesTable = JBTable(dataModel)
                packagesTable.selectionModel.addListSelectionListener {
                    val selectedRow = packagesTable.selectedRow
                    if (selectedRow != -1) {
                        val name = packagesTable.getValueAt(selectedRow, 0) as String
                        val version = packagesTable.getValueAt(selectedRow, 1) as String
                        secondComponentPanel.removeAll()
                        val packageNameLabel = JLabel("$name/$version").apply {
                            font = font.deriveFont(Font.BOLD, 18f) // set font size to 18 and bold
                        }
                        secondComponentPanel.add(packageNameLabel, BorderLayout.NORTH)

                        val installButtonPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                            val installButton = JButton("Install").apply {
                                addActionListener {
                                    try {
                                        val processBuilder = ProcessBuilder("conan --version")
                                        val process = processBuilder.start()
                                        process.waitFor() // Wait for the process to finish
                                        thisLogger().warn("call to install")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            add(installButton)
                        }

                        secondComponentPanel.add(installButtonPanel)

                        secondComponentPanel.revalidate()
                        secondComponentPanel.repaint()
                    }
                }

                val scrollablePane = JBScrollPane(packagesTable)

                add(actionToolbar.component, BorderLayout.NORTH)
                add(scrollablePane, BorderLayout.CENTER)
            }
            secondComponent = secondComponentPanel.apply { withEmptyText("No selection") }
            proportion = 0.28f
        }

    }
}
