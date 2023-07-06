package com.jfrog.conan.clionplugin.dialogs

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.jfrog.conan.clionplugin.bundles.UIBundle
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
import com.jfrog.conan.clionplugin.toolWindow.ReadmePanel
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class ConanInstallHintDialog(val project: Project, val name: String) : DialogWrapper(project, true) {
    private val readmePanel = ReadmePanel()
    private val properties = project.service<PropertiesComponent>()

    init {
        init()
        title = UIBundle.message("inspect.title")
    }

    override fun createSouthPanel(): JComponent {
        setDoNotAskOption(object : com.intellij.openapi.ui.DoNotAskOption {
            override fun isToBeShown(): Boolean {
                return !properties.getBoolean(PersistentStorageKeys.DONT_SHOW_FIND_PACKAGE_HINT)
            }

            override fun setToBeShown(toBeShown: Boolean, exitCode: Int) {
                properties.setValue(PersistentStorageKeys.DONT_SHOW_FIND_PACKAGE_HINT, !toBeShown)
            }

            override fun canBeHidden(): Boolean {
                return true
            }

            override fun shouldSaveOptionsOnCancel(): Boolean {
                return true
            }

            override fun getDoNotShowMessage(): String {
                return UIBundle.message("library.added.checkbox")
            }

        })
        return super.createSouthPanel()
    }

    override fun createCenterPanel(): JComponent {
        val contentPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        contentPanel.add(readmePanel.getHTMLPackageInfo(name))
        return JBScrollPane(
            contentPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        )
    }
}
