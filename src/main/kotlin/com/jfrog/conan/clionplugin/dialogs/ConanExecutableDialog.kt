package com.jfrog.conan.clionplugin.dialogs

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*


object ConanExecutableChooserDescriptor : FileChooserDescriptor(true, true, false, false, false, false) {

    init {
        withFileFilter { it.isConanExecutable }
        withTitle("Select Conan executable")
    }

    override fun isFileSelectable(file: VirtualFile?): Boolean {
        return super.isFileSelectable(file) && file != null && !file.isDirectory
    }
}

val VirtualFile.isConanExecutable: Boolean get() {
    return (extension == null || extension == "exe") && name == "conan"
}

class ConanExecutableDialogWrapper(project: Project) : DialogWrapper(true) {
    private val properties = project.service<PropertiesComponent>()

    private val fileChooserField1 = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            "Conan executable",
            "Conan executable",
            project,
            ConanExecutableChooserDescriptor
        )
        text = properties.getValue("com.jfrog.conanplugin.conanexecutable", "")
    }

    private val field2 = JTextField(properties.getValue("com.jfrog.conanplugin.field2", ""))
    private val field3 = JTextField(properties.getValue("com.jfrog.conanplugin.field3", ""))

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
        panel.add(JLabel("Conan executable"), gbcLabel)
        panel.add(fileChooserField1, gbcField)
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
        properties.setValue("com.jfrog.conanplugin.conanexecutable", fileChooserField1.text)
        properties.setValue("com.jfrog.conanplugin.field2", field2.text)
        properties.setValue("com.jfrog.conanplugin.field3", field3.text)
        super.doOKAction()
    }
}