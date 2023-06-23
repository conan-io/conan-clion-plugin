package com.jfrog.conan.clionplugin.toolWindow

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jfrog.conan.clionplugin.bundles.UIBundle
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.conan.Conan
import com.jfrog.conan.clionplugin.dialogs.ConanExecutableDialogWrapper
import com.jfrog.conan.clionplugin.dialogs.ConanInspectPackagesDialogWrapper
import com.jfrog.conan.clionplugin.services.ConanService
import com.jfrog.conan.clionplugin.services.RemotesDataStateService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainActionToolbar(val project: Project) {
    private val stateService = project.service<RemotesDataStateService>()
    private val conanService = project.service<ConanService>()


    private fun getConfigureAction(): AnAction {
        return object : AnAction(
            UIBundle.message("toolbar.action.show.dialog.configure"),
            null,
            AllIcons.General.Settings
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                ConanExecutableDialogWrapper(project).showAndGet()
            }
        }
    }

    private fun getAddConanSupportAction(): AnAction {
        return object :
            AnAction(UIBundle.message("toolbar.action.add.conan.support"), null, AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {
                val cmake = CMake(project)
                cmake.addConanSupport()
            }
        }
    }

    private fun getUpdateAction(): AnAction {
        return object :
            AnAction(UIBundle.message("toolbar.action.update"), null, AllIcons.Actions.Refresh) {
            override fun actionPerformed(e: AnActionEvent) {

                conanService.downloadCMakeProvider(true)

                Conan(project).list("*") { runOutput ->
                    if (runOutput.exitCode == 0) {
                        val newJson = Json.decodeFromString<RemotesDataStateService.State>(runOutput.stdout)
                        stateService.loadState(newJson)
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("com.jfrog.conan.clionplugin.notifications.general")
                            .createNotification(
                                UIBundle.message("update.successful.title"),
                                UIBundle.message("update.successful.body"),
                                NotificationType.INFORMATION
                            )
                            .notify(project)
                    } else {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("com.jfrog.conan.clionplugin.notifications.general")
                            .createNotification(
                                UIBundle.message("update.error.title"),
                                UIBundle.message("update.error.body"),
                                NotificationType.ERROR
                            )
                            .notify(project)
                    }
                }
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
        }
    }

    fun getContent(): ActionToolbar {
        val actionGroup = DefaultActionGroup().apply {
            add(getConfigureAction())
            add(getAddConanSupportAction())
            add(getUpdateAction())
            add(getShowUsedPackagesAction())
        }
        return ActionManager.getInstance().createActionToolbar("ConanToolbar", actionGroup, true)
    }
}
