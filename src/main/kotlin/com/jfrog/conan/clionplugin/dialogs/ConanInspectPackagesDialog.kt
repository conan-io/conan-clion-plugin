package com.jfrog.conan.clionplugin.dialogs

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.jfrog.conan.clionplugin.services.ConanService
import java.io.File
import javax.swing.JComponent
import javax.swing.JTextArea
import javax.swing.border.EmptyBorder

class ConanInspectPackagesDialogWrapper(val project: Project) : DialogWrapper(true) {

    init {
        init()
        title = "Packages used by the project"
    }

    override fun createCenterPanel(): JComponent? {
        val textArea = JTextArea()
        textArea.isEditable = false
        textArea.border = EmptyBorder(10, 10, 10, 10)
        val dependencyFile = File(project.service<ConanService>().getCMakeWorkspace().projectPath.toString(), "conandata.yml")
        val requirements = parseRequirements(dependencyFile)

        textArea.text = requirements.joinToString("\n")

        return textArea
    }

    private fun parseRequirements(file: File): List<String> {
        val requirements = mutableListOf<String>()
        val lines = file.readLines()

        var startReading = false
        for (line in lines) {
            if (line.trim() == "requirements:") {
                startReading = true
                continue
            }

            if (startReading && line.trim().startsWith("-")) {
                requirements.add(line.substringAfter("-").trim().replace("\"", ""))
            }
        }

        return requirements
    }
}
