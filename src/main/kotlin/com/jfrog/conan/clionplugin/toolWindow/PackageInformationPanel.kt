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
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.*


class PackageInformationPanel(private val conanService: ConanService, private val readmePanel: ReadmePanel) : JBPanelWithEmptyText() {
    private val versionModel = DefaultComboBoxModel<String>()

    init {
        layout = GridBagLayout()
        alignmentX = Component.LEFT_ALIGNMENT
    }

    fun getTitle(name: String): JBLabel {
        return JBLabel(readmePanel.getTitleHtml(name)).apply {
            alignmentX = Component.LEFT_ALIGNMENT
        }
    }

    fun updatePanel(name: String, versions: List<String>) {

        versionModel.apply {
            removeAllElements()
            addAll(versions)
            selectFirst()
        }
        removeAll()

        val c = GridBagConstraints()
        c.anchor = GridBagConstraints.NORTHWEST

        c.fill = GridBagConstraints.HORIZONTAL
        c.gridx = 0
        c.gridy = 0
        add(getTitle(name), c)

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

        c.gridx = 0
        c.gridy = 1
        add(buttonsPanel, c)

        c.fill = GridBagConstraints.BOTH
        c.weighty = 1.0
        c.weightx = 1.0
        c.gridx = 0
        c.gridy = 2

        val myHtmlPanel = JPanel().apply {
            add(readmePanel.getHTMLPackageInfo(name))
        }

        val contentPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        contentPanel.add(readmePanel.getHTMLPackageInfo(name))

        val scrollPane = JBScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

        add(scrollPane, c)

        revalidate()
        repaint()
    }
}
