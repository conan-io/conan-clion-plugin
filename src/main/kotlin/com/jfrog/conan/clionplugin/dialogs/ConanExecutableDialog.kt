package com.jfrog.conan.clionplugin.dialogs

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.JBUI
import com.jfrog.conan.clionplugin.cmake.CMake
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

class ConanExecutableDialogWrapper(val project: Project) : DialogWrapper(true) {
    private val properties = project.service<PropertiesComponent>()
    private val cmake = CMake(project)
    private val profileCheckboxes:  MutableList<JBCheckBox> = mutableListOf()

    private val fileChooserField1 = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            "Conan executable",
            "Conan executable",
            project,
            ConanExecutableChooserDescriptor
        )
        if (useConanFromSystemCheckBox.isSelected) {
            text = ""
        }
        else {
            text = properties.getValue(PersistentStorageKeys.CONAN_EXECUTABLE, "")

        }
    }

    private val useConanFromSystemCheckBox = JCheckBox("Use conan installed in the system").apply {
        val conanExe = properties.getValue(PersistentStorageKeys.CONAN_EXECUTABLE, "")
        isSelected = conanExe == "conan"
        addActionListener {
            fileChooserField1.isEnabled = !isSelected
            if (!isSelected) {
                properties.setValue(PersistentStorageKeys.CONAN_EXECUTABLE, "")
            }
        }
    }

    // TODO: Still pending to detect when a profile is added, then setting the Conan configuration for the profile
    private val automaticallyAddCheckbox = JCheckBox("Automatically add Conan support for all configurations").apply {
        val selected = properties.getValue(PersistentStorageKeys.AUTOMATIC_ADD_CONAN, "false")
        isSelected = selected == "true"
    }

    init {
        init()
        title = "Configuration"
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbcLabel = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            weightx = 0.0
            weighty = 0.0
            insets = JBUI.insetsTop(10)
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
        val newCheckConstraint = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            weighty = 0.0
            gridwidth = GridBagConstraints.REMAINDER
            insets = JBUI.insetsTop(10)
        }
        val gbcPlaceholder = GridBagConstraints().apply {
            fill = GridBagConstraints.VERTICAL
            weighty = 1.0
            gridwidth = GridBagConstraints.REMAINDER
        }

        panel.add(JLabel("Conan executable"), gbcLabel)
        panel.add(fileChooserField1, gbcField)
        panel.add(useConanFromSystemCheckBox, newCheckConstraint)

        val checkboxPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        val configurationsLabel = JLabel("Use Conan for the following configurations:").apply {
            border = JBUI.Borders.emptyTop(10)
        }

        checkboxPanel.add(configurationsLabel)

        cmake.getActiveProfiles().forEach { profile ->
            val selected = cmake.checkConanUsedInProfile(profile.name)
            val checkbox = JBCheckBox(profile.name, selected)
            profileCheckboxes.add(checkbox)
            checkboxPanel.add(checkbox)
        }

        panel.add(checkboxPanel, gbcCheckboxPanel)

        panel.add(automaticallyAddCheckbox, newCheckConstraint)

        panel.add(Box.createVerticalGlue(), gbcPlaceholder)

        return panel
    }

    override fun doOKAction() {
        if (!useConanFromSystemCheckBox.isSelected) {
            properties.setValue(PersistentStorageKeys.CONAN_EXECUTABLE, fileChooserField1.text)
        }
        else {
            properties.setValue(PersistentStorageKeys.CONAN_EXECUTABLE, "conan")
        }
        val selected = if (automaticallyAddCheckbox.isSelected) "true" else "false"
        properties.setValue(PersistentStorageKeys.AUTOMATIC_ADD_CONAN, selected)

        profileCheckboxes.forEach { checkbox ->
            val profileName = checkbox.text
            if (checkbox.isSelected) {
                cmake.injectDependencyProviderToProfile(profileName)
            } else {
                cmake.removeDependencyProviderFromProfile(profileName)
            }
        }

        super.doOKAction()
    }
}
