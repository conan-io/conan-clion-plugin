package com.jfrog.conan.clionplugin.dialogs

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.JBUI
import com.jfrog.conan.clionplugin.bundles.UIBundle
import com.jfrog.conan.clionplugin.services.ConanService
import javax.swing.JComponent
import javax.swing.JTextArea

class ConanInspectPackagesDialogWrapper(val project: Project) : DialogWrapper(true) {

    init {
        init()
        title = UIBundle.message("inspect.title")
    }

    override fun createCenterPanel(): JComponent {
        val textArea = JTextArea()
        textArea.isEditable = false
        textArea.border = JBUI.Borders.empty(10)
        val requirements = project.service<ConanService>().getRequirements()

        textArea.text = requirements.joinToString("\n")

        return textArea
    }


}
