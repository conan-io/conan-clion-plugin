package com.jfrog.conan.clionplugin.cmake.extensions


import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.CMakeRunnerStep
import com.jetbrains.cidr.cpp.cmake.CMakeRunnerStep.Parameters
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.rd.util.string.printToString
import com.jfrog.conan.clionplugin.bundles.NotificationsBundle
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys

@Service(Service.Level.PROJECT)
class ConanCMakeRunnerStep : CMakeRunnerStep {
    override fun beforeGeneration(project: Project, parameters: Parameters) {
        val profileName = parameters.getUserData(Parameters.PROFILE_NAME)
        val cmakeSettings = CMakeSettings.getInstance(project)
        val profile = cmakeSettings.profiles.find { it.name == profileName }

        if (project.service<PropertiesComponent>()
                .getBoolean(PersistentStorageKeys.AUTOMANAGE_CMAKE_ADVANCED_SETTINGS)
        ) {
            AdvancedSettings.setBoolean("cmake.reload.profiles.sequentially", true)
            NotificationGroupManager.getInstance()
                .getNotificationGroup("com.jfrog.conan.clionplugin.notifications.general")
                .createNotification(
                    NotificationsBundle.message("cmake.parallel.autoactivated.title"),
                    NotificationsBundle.message("cmake.parallel.autoactivated.body"),
                    NotificationType.INFORMATION
                )
                .notify(project)
        } else if (!AdvancedSettings.getBoolean("cmake.reload.profiles.sequentially") && listOf(1).size > 1) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("com.jfrog.conan.clionplugin.notifications.general")
                .createNotification(
                    NotificationsBundle.message("cmake.parallel.notactivated.title"),
                    NotificationsBundle.message("cmake.parallel.notactivated.body"),
                    NotificationType.WARNING
                )
                .notify(project)
        }
        println(profile.printToString())
    }

    override fun modifyParameters(project: Project, parameters: Parameters): Parameters {
        println("modifyParameters")
        val profileName = parameters.getUserData(Parameters.PROFILE_NAME)
        val cmakeSettings = CMakeSettings.getInstance(project)
        val profile = cmakeSettings.profiles.find { it.name == profileName }
        println(profile.printToString())
        return parameters
    }
}
