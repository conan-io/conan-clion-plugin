package com.jfrog.conan.clionplugin.dialogs

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*
import javax.swing.border.EmptyBorder

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

class ConanExecutableDialogWrapper(val project: Project) : DialogWrapper(true) {
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
        val selected = properties.getValue(PersistentStorageKeys.AUTOMATIC_ADD_CONAN, "true")
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
            weightx = 0.0
            weighty = 0.0
            insets = Insets(10, 0, 0, 0)
        }
        val gbcField = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            weighty = 0.0
            gridwidth = GridBagConstraints.REMAINDER
        }
        val gbcCheckboxPanel = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.BOTH
            weightx = 1.0
            weighty = 1.0
            gridwidth = GridBagConstraints.REMAINDER
        }
        val gbcAutomaticallyAddCheckbox = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            weighty = 0.0
            gridwidth = GridBagConstraints.REMAINDER
            insets = Insets(10, 0, 0, 0)
        }
        val gbcPlaceholder = GridBagConstraints().apply {
            fill = GridBagConstraints.VERTICAL
            weighty = 1.0
            gridwidth = GridBagConstraints.REMAINDER
        }

        panel.add(JLabel("Conan executable"), gbcLabel)
        panel.add(fileChooserField1, gbcField)

        val cmake = CMake(project)
        val selectedBuildConfiguration = cmake.getSelectedBuildConfiguration()

        val checkboxPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        val configurationsLabel = JLabel("Use Conan for the following configurations:").apply {
            border = EmptyBorder(10, 0, 0, 0)
        }

        checkboxPanel.add(configurationsLabel)

        cmake.getActiveProfiles().forEach {
            // TODO: here we have to check if the dependency provider is already applied to the profile
            // let the user decide to deactivate it or not for certain profiles
            val checkbox = JBCheckBox(it.name, it.name == selectedBuildConfiguration?.profileName)
            checkboxPanel.add(checkbox)
        }

        panel.add(checkboxPanel, gbcCheckboxPanel)

        panel.add(automaticallyAddCheckbox, gbcAutomaticallyAddCheckbox)

        panel.add(Box.createVerticalGlue(), gbcPlaceholder)

        return panel
    }

    override fun doOKAction() {
        properties.setValue(PersistentStorageKeys.CONAN_EXECUTABLE, fileChooserField1.text)
        val selected = if (automaticallyAddCheckbox.isSelected) "true" else "false"
        properties.setValue(PersistentStorageKeys.AUTOMATIC_ADD_CONAN, selected)
        super.doOKAction()
    }
}
