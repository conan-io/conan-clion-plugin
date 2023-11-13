package com.jfrog.conan.clion.dialogs

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.jfrog.conan.clion.bundles.UIBundle
import com.jfrog.conan.clion.services.ConanService
import com.jfrog.conan.clion.toolWindow.UsedPackagesPanel
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class ConanInspectPackagesDialogWrapper(val project: Project) : DialogWrapper(true) {

    private val usedPackagesPanel = UsedPackagesPanel(project)
    init {
        init()
        title = UIBundle.message("inspect.title")
    }

    override fun createCenterPanel(): JComponent {
        val requirements = project.service<ConanService>().getRequirements()
        val contentPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        contentPanel.add(usedPackagesPanel.getHTMLUsedPackages(requirements))
        return JBScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
    }


}
