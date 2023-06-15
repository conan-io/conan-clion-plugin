package com.jfrog.conan.clionplugin.dialogs

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
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

val VirtualFile.isConanExecutable: Boolean
    get() {
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
        text = properties.getValue(PersistentStorageKeys.CONAN_EXECUTABLE, "")
    }

    private val automaticallyAddCheckbox = JCheckBox("Automatically add Conan support for all configurations").apply {
        val selected = properties.getValue(PersistentStorageKeys.ADD_CONAN_SUPPORT, "true")
        isSelected = selected == "true"
    }

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
        panel.add(automaticallyAddCheckbox, gbcField)

        // Use GridBagConstraints to create a placeholder component that will take all remaining vertical space
        gbcField.weighty = 1.0
        panel.add(Box.createVerticalGlue(), gbcField)

        return panel
    }

    override fun doOKAction() {
        properties.setValue(PersistentStorageKeys.CONAN_EXECUTABLE, fileChooserField1.text)
        val selected = if (automaticallyAddCheckbox.isSelected) "true" else "false"
        properties.setValue(PersistentStorageKeys.ADD_CONAN_SUPPORT, selected)
        super.doOKAction()
    }
}
