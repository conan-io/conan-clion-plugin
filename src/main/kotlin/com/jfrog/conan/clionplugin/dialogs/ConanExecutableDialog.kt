package com.jfrog.conan.clionplugin.dialogs

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.observable.util.whenTextChanged
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.jfrog.conan.clionplugin.bundles.UIBundle
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
import com.jfrog.conan.clionplugin.services.ConanService
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

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
    private val conanService = project.service<ConanService>()
    private val cmake = CMake(project)
    private val profileCheckboxes: MutableList<JBCheckBox> = mutableListOf()

    private val conanExecutablePathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            UIBundle.message("config.file.selector.title"),
            UIBundle.message("config.file.selector.description"),
            project,
            ConanExecutableChooserDescriptor
        )
    }

    private val automaticallyAddCheckbox =
        JBCheckBox(UIBundle.message("config.automanage.cmake.integrations")).apply {
            isSelected = isFirstSetup() || properties.getValue(PersistentStorageKeys.AUTOMATIC_ADD_CONAN, "false") == "true"
        }

    private val useConanFromSystemCheckBox = JBCheckBox(UIBundle.message("config.use.system.conan")).apply {
        val conanExe = properties.getValue(PersistentStorageKeys.CONAN_EXECUTABLE, "")
        isSelected = conanExe == "conan"
        conanExecutablePathField.text = if (isSelected) "" else properties.getValue(PersistentStorageKeys.CONAN_EXECUTABLE, "")
        conanExecutablePathField.isEnabled = !isSelected
        addActionListener {
            conanExecutablePathField.isEnabled = !isSelected
        }
    }

    private val checkboxAdvancedSetting = JBCheckBox(
        UIBundle.message("config.automanage.cmake.parallel"),
        isFirstSetup() || properties
            .getBoolean(PersistentStorageKeys.AUTOMANAGE_CMAKE_ADVANCED_SETTINGS)
    )

    init {
        init()
        title = UIBundle.message("config.title")
    }

    private fun updateOkButtonState() {
        val isSystemConanSelected = useConanFromSystemCheckBox.isSelected
        val isFileChooserNotEmpty = conanExecutablePathField.text.isNotEmpty()
        okAction.isEnabled = isSystemConanSelected || isFileChooserNotEmpty
    }

    private fun isFirstSetup(): Boolean {
        return !properties.isValueSet(PersistentStorageKeys.HAS_BEEN_SETUP) || !properties.getBoolean(PersistentStorageKeys.HAS_BEEN_SETUP)
    }

    override fun createCenterPanel(): JComponent {
        return JPanel(GridBagLayout()).apply {
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

            add(JBLabel(UIBundle.message("config.executable")), gbcLabel)
            add(conanExecutablePathField, gbcField)
            add(useConanFromSystemCheckBox, newCheckConstraint)


            conanExecutablePathField.addActionListener {
                updateOkButtonState()
                updateOkButtonState()
            }
            conanExecutablePathField.textField.whenTextChanged {
                updateOkButtonState()
            }

            useConanFromSystemCheckBox.addActionListener {
                updateOkButtonState()
            }

            val checkboxPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
            }

            val configurationsLabel = JBLabel(UIBundle.message("config.configurations.use")).apply {
                border = JBUI.Borders.emptyTop(10)
            }

            checkboxPanel.add(configurationsLabel)
            val firstSetup = isFirstSetup()

            cmake.getActiveProfiles().forEach { profile ->
                val selected = cmake.checkConanUsedInProfile(profile.name)
                val checkbox = JBCheckBox(profile.name, firstSetup || selected)
                profileCheckboxes.add(checkbox)
                checkboxPanel.add(checkbox)
            }

            add(checkboxPanel, gbcCheckboxPanel)
            add(automaticallyAddCheckbox, newCheckConstraint)
            add(checkboxAdvancedSetting, newCheckConstraint)
            updateOkButtonState()
        }
    }

    override fun doOKAction() {
        val firstSetup = !properties.getBoolean(PersistentStorageKeys.HAS_BEEN_SETUP, false)

        properties.setValue(PersistentStorageKeys.HAS_BEEN_SETUP, true)
        if (!useConanFromSystemCheckBox.isSelected) {
            properties.setValue(PersistentStorageKeys.CONAN_EXECUTABLE, conanExecutablePathField.text)
        }
        else {
            properties.setValue(PersistentStorageKeys.CONAN_EXECUTABLE, "conan")
        }
        val selected = if (automaticallyAddCheckbox.isSelected) "true" else "false"
        properties.setValue(PersistentStorageKeys.AUTOMATIC_ADD_CONAN, selected)

        if (firstSetup) {
            conanService.downloadCMakeProvider()
            conanService.downloadLibraryData()
        }

        profileCheckboxes.forEach { checkbox ->
            val profileName = checkbox.text
            if (checkbox.isSelected) {
                cmake.injectDependencyProviderToProfile(profileName)
            } else {
                cmake.removeDependencyProviderFromProfile(profileName)
            }
        }
        properties
            .setValue(PersistentStorageKeys.AUTOMANAGE_CMAKE_ADVANCED_SETTINGS, checkboxAdvancedSetting.isSelected)
        conanService.fireOnConfiguredListeners(conanService.isPluginConfigured())

        super.doOKAction()
    }

}
