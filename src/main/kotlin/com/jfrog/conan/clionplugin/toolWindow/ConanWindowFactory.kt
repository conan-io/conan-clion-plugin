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
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.jfrog.conan.clionplugin.services.MyProjectService
import java.awt.*
import java.io.File
import java.nio.file.Paths
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel

data class ConfigData(val field1: String, val field2: String, val field3: String)

val filePath = Paths.get(System.getProperty("user.home"), "myPluginConfig.txt").toString()

fun loadConfig(): ConfigData {
    val file = File(filePath)
    if (!file.exists()) {
        return ConfigData("", "", "")
    }
    val map = file.readLines().associate {
        val (key, value) = it.split("=")
        key to value
    }
    return ConfigData(map["field1"] ?: "", map["field2"] ?: "", map["field3"] ?: "")
}

fun saveConfig(data: ConfigData) {
    val file = File(filePath)
    val content = "field1=${data.field1}\nfield2=${data.field2}\nfield3=${data.field3}"
    file.writeText(content)
}

class MyDialogWrapper : DialogWrapper(true) {
    val configData = loadConfig()
    val field1 = JTextField(configData.field1)
    val field2 = JTextField(configData.field2)
    val field3 = JTextField(configData.field3)

    init {
        init()
        title = "Configuration"
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(GridBagLayout())
        val gbcLabel = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.NONE
            weightx = 0.0
            weighty = 0.0
        }
        val gbcField = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            weighty = 0.0
            gridwidth = GridBagConstraints.REMAINDER
        }

        panel.add(JLabel("Field 1"), gbcLabel)
        panel.add(field1, gbcField)
        panel.add(JLabel("Field 2"), gbcLabel)
        panel.add(field2, gbcField)
        panel.add(JLabel("Field 3"), gbcLabel)
        panel.add(field3, gbcField)

        // Use GridBagConstraints to create a placeholder component that will take all remaining vertical space
        gbcField.weighty = 1.0
        panel.add(Box.createVerticalGlue(), gbcField)

        return panel
    }


    override fun doOKAction() {
        saveConfig(ConfigData(field1.text, field2.text, field3.text))
        super.doOKAction()
    }
}

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
                            MyDialogWrapper().showAndGet()
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
