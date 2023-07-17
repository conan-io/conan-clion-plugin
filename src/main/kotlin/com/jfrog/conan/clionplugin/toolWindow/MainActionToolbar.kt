package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.bundles.UIBundle
import com.jfrog.conan.clionplugin.dialogs.ConanExecutableDialogWrapper
import com.jfrog.conan.clionplugin.dialogs.ConanInspectPackagesDialogWrapper
import com.jfrog.conan.clionplugin.services.ConanService

class MainActionToolbar(val project: Project) {
    private val conanService = project.service<ConanService>()


    private fun getConfigureAction(): AnAction {
        return object : AnAction(
            UIBundle.message("toolbar.action.show.dialog.configure"),
            null,
            AllIcons.General.Settings
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                ConanExecutableDialogWrapper(project).showAndGet()
                CMake(project).handleAdvancedSettings()
            }
        }
    }

    private fun getUpdateAction(): AnAction {
        return object :
            AnAction(UIBundle.message("toolbar.action.update"), null, AllIcons.Actions.Refresh) {
            override fun actionPerformed(e: AnActionEvent) {

                conanService.downloadCMakeProvider(true)
                conanService.downloadLibraryData(true)
            }

            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = conanService.isPluginConfigured()
                super.update(e)
            }
        }
    }

    private fun getShowUsedPackagesAction(): AnAction {
        return object : AnAction(
            UIBundle.message("toolbar.action.show.used.packages"),
            null,
            AllIcons.General.InspectionsEye
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                ConanInspectPackagesDialogWrapper(project).showAndGet()
            }

            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = conanService.isPluginConfigured()
                super.update(e)
            }
        }
    }

    fun getContent(): ActionToolbar {
        val actionGroup = DefaultActionGroup().apply {
            val configureAction = getConfigureAction()
            val updateAction = getUpdateAction()
            val showUsedPackagesAction = getShowUsedPackagesAction()
            add(configureAction)
            add(updateAction)
            add(showUsedPackagesAction)
        }
        return ActionManager.getInstance().createActionToolbar("ConanToolbar", actionGroup, true).apply {
            component.repaint()
        }
    }
}
