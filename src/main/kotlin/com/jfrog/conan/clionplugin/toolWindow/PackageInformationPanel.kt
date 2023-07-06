package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.collaboration.ui.selectFirst
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.JBScrollPane
import com.jfrog.conan.clionplugin.bundles.UIBundle
import com.jfrog.conan.clionplugin.dialogs.ConanInstallHintDialog
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
import com.jfrog.conan.clionplugin.services.ConanService
import java.awt.Component
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane


class PackageInformationPanel(
    private val project: Project,
    private val conanService: ConanService,
    private val readmePanel: ReadmePanel
) : JBPanelWithEmptyText() {
    private val versionModel = DefaultComboBoxModel<String>()

    init {
        layout = GridBagLayout()
        alignmentX = Component.LEFT_ALIGNMENT
    }

    private fun getTitle(name: String): JBLabel {
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
                val selectedVersion = comboBox.selectedItem as String
                conanService.runUseFlow(name, selectedVersion)
                val isRequired = conanService.getRequirements().any { it.startsWith("$name/") }
                addButton.isVisible = !isRequired
                removeButton.isVisible = isRequired
                comboBox.isEnabled = !isRequired
                comboBox.toolTipText = UIBundle.message("library.description.combo.disabled")

                val properties = project.service<PropertiesComponent>()

                if (properties.getBoolean(PersistentStorageKeys.DONT_SHOW_FIND_PACKAGE_HINT, false)) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("com.jfrog.conan.clionplugin.notifications.general")
                        .createNotification(
                            UIBundle.message("library.added.title"),
                            UIBundle.message("library.added.body", "$name/$selectedVersion"),
                            NotificationType.INFORMATION
                        )
                        .notify(project)
                } else {
                    ConanInstallHintDialog(project, name).show()
                }
            }

            removeButton.addActionListener {
                val selectedVersion = comboBox.selectedItem as String
                conanService.runRemoveRequirementFlow(name, selectedVersion)
                val isRequired = conanService.getRequirements().any { it.startsWith("$name/") }
                addButton.isVisible = !isRequired
                removeButton.isVisible = isRequired
                comboBox.isEnabled = !isRequired
                comboBox.toolTipText = null
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("com.jfrog.conan.clionplugin.notifications.general")
                    .createNotification(
                        UIBundle.message("library.removed.title"),
                        UIBundle.message("library.removed.body", "$name/$selectedVersion"),
                        NotificationType.INFORMATION
                    )
                    .notify(project)
            }

            val isRequired = conanService.getRequirements().any { it.startsWith("$name/") }
            addButton.isVisible = !isRequired
            removeButton.isVisible = isRequired
            comboBox.isEnabled = !isRequired

            if (isRequired) {
                val requirement = conanService.getRequirements().find { it.startsWith("$name/") }
                val version = requirement?.split("/")?.get(1)
                comboBox.selectedItem = version
                comboBox.setToolTipText(UIBundle.message("library.description.combo.disabled"))
            } else {
                comboBox.setToolTipText(null)
            }
        }

        c.gridx = 0
        c.gridy = 1
        add(buttonsPanel, c)

        c.fill = GridBagConstraints.BOTH
        c.weighty = 1.0
        c.weightx = 1.0
        c.gridx = 0
        c.gridy = 2

        val contentPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        contentPanel.add(readmePanel.getHTMLPackageInfo(name))

        val scrollPane =
            JBScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

        add(scrollPane, c)

        revalidate()
        repaint()
    }
}
