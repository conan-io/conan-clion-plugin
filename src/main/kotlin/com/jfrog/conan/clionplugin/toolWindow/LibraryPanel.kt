package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.collaboration.ui.selectFirst
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.jfrog.conan.clionplugin.bundles.UIBundle
import com.jfrog.conan.clionplugin.services.ConanService
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.*


class LibraryPanel(private val conanService: ConanService, private val packageInfo: PackageInfoPanel) : JBPanelWithEmptyText() {
    private val versionModel = DefaultComboBoxModel<String>()

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    fun updatePanel(name: String, versions: List<String>) {

        versionModel.apply {
            removeAllElements()
            addAll(versions)
            selectFirst()
        }
        removeAll()

        add(JBLabel(packageInfo.getTitleHtml(name)).apply {
            alignmentX = Component.LEFT_ALIGNMENT
        })

        val buttonsPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            alignmentX = Component.LEFT_ALIGNMENT

            val comboBox = ComboBox(versionModel)
            add(comboBox)

            val addButton = JButton(UIBundle.message("library.description.button.install"))
            val removeButton = JButton(UIBundle.message("library.description.button.remove"))

            add(addButton)
            add(removeButton)

            addButton.addActionListener {
                conanService.runUseFlow(name, comboBox.selectedItem as String)
                val isRequired = conanService.getRequirements().any { it.startsWith("$name/") }
                addButton.isVisible = !isRequired
                removeButton.isVisible = isRequired
                Messages.showMessageDialog(
                        UIBundle.message("library.added.body", comboBox.selectedItem as String),
                        UIBundle.message("library.added.title"),
                        Messages.getInformationIcon()
                )
            }

            removeButton.addActionListener {
                conanService.runRemoveRequirementFlow(name, comboBox.selectedItem as String)
                val isRequired = conanService.getRequirements().any { it.startsWith("$name/") }
                addButton.isVisible = !isRequired
                removeButton.isVisible = isRequired
                Messages.showMessageDialog(
                        UIBundle.message("library.removed.body", comboBox.selectedItem as String),
                        UIBundle.message("library.removed.title"),
                        Messages.getInformationIcon()
                )
            }

            val isRequired = conanService.getRequirements().any { it.startsWith("$name/") }
            addButton.isVisible = !isRequired
            removeButton.isVisible = isRequired
        }
        add(buttonsPanel)

        val scrollPane = JBScrollPane(JPanel().apply {
            alignmentX = Component.LEFT_ALIGNMENT
            add(packageInfo.getHTMLPackageInfo(name))
        }, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            add(scrollPane)
        })

        revalidate()
        repaint()
    }
}
