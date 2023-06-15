package com.jfrog.conan.clionplugin.dialogs

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBCheckBoxMenuItem
import com.intellij.ui.components.JBLabel
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*


class ConanInstallDialogWrapper(val project: Project) : DialogWrapper(true) {
    private val properties = project.service<PropertiesComponent>()
    private val checkboxes = mutableMapOf<String, JBCheckBox>()

    init {
        init()
        title = "Choose which profiles to install this dependency for"
    }

    fun getSelectedInstallProfiles(): List<String> {
        return checkboxes.mapNotNull {
            if (it.value.isSelected) it.key
            else null
        }
    }

    fun getUnselectedInstallProfiles(): List<String> {
        return checkboxes.mapNotNull {
            if (!it.value.isSelected) it.key
            else null
        }
    }

    override fun createCenterPanel(): JComponent? {
        return JPanel(GridBagLayout()).apply {

            val gbcField = GridBagConstraints().apply {
                anchor = GridBagConstraints.WEST
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                weighty = 0.0
                gridwidth = GridBagConstraints.REMAINDER
            }

            checkboxes.clear()
            val cmake = CMake(project)
            val selectedBuildConfiguration = cmake.getSelectedBuildConfiguration()
            add(JBCheckBox("Install for all configurations").apply {
                font = font.deriveFont(Font.BOLD)
                addActionListener {
                    checkboxes.forEach { (_, control) ->
                        control.isSelected = this.isSelected
                    }
                }
            }, gbcField)

            cmake.getActiveProfiles().forEach {
                val checkbox = JBCheckBox(it.name, it.name == selectedBuildConfiguration?.profileName)
                checkboxes[it.name] = checkbox
                add(checkbox, gbcField)
            }


            gbcField.weighty = 1.0
            add(Box.createVerticalGlue(), gbcField)
        }
    }

    override fun doOKAction() {
        super.doOKAction()
    }
}