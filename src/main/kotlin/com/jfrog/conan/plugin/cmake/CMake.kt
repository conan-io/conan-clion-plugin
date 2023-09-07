package com.jfrog.conan.plugin.cmake

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.cidr.cpp.cmake.model.CMakeConfiguration
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration
import com.jfrog.conan.plugin.bundles.UIBundle
import com.jfrog.conan.plugin.models.PersistentStorageKeys
import com.jfrog.conan.plugin.services.ConanService

class CMake(val project: Project) {

    fun checkConanUsedInAnyActiveProfile(): Boolean {
        return getActiveProfiles().any { checkConanUsedInProfile(it.name) }
    }

    fun checkConanUsedInProfile(profileName: String?): Boolean {
        val cmakeSettings = CMakeSettings.getInstance(project)
        val profiles = cmakeSettings.profiles

        for (profile in profiles) {
            if (profile.name == profileName) {
                val existingGenerationOptions = profile.generationOptions ?: ""
                return listOf("CONAN_COMMAND", "conan_provider.cmake").any { existingGenerationOptions.contains(it) }
            }
        }
        return false
    }

    fun injectConanSupportToProfile(profileName: String) {
        val conanExecutable: String = project.service<PropertiesComponent>().getValue(
            PersistentStorageKeys.CONAN_EXECUTABLE,
            ""
        )
        val generationOptions: MutableList<String> = mutableListOf()
        val conanProviderPath = project.service<ConanService>().getCMakeProviderFilename()
        generationOptions.add("-DCMAKE_PROJECT_TOP_LEVEL_INCLUDES=\"${conanProviderPath}\"")
        if (conanExecutable != "" && conanExecutable != "conan") {
            generationOptions.add("-DCONAN_COMMAND=\"${conanExecutable}\"")
        }
        else {
            removeGenerationOptions(
                profileName,
                listOf("CONAN_COMMAND")
            )
        }
        addGenerationOptions(profileName, generationOptions)
    }

    fun removeConanSupportFromProfile(profileName: String) {
        removeGenerationOptions(
            profileName,
            listOf(
                "CMAKE_PROJECT_TOP_LEVEL_INCLUDES",
                "CONAN_COMMAND"
            )
        )
    }

    private fun addGenerationOptions(profileName: String?, generationOptions: List<String>) {
        val cmakeSettings = CMakeSettings.getInstance(project)
        val profiles = cmakeSettings.profiles
        val modifiedProfiles: MutableList<CMakeSettings.Profile> = mutableListOf()

        for (profile in profiles) {
            if (profile.name == profileName) {
                val existingGenerationOptions = profile.generationOptions ?: ""
                val newGenerationOptions = if (existingGenerationOptions.isNotEmpty()) {
                    existingGenerationOptions.split(" ").toMutableList()
                } else {
                    mutableListOf<String>()
                }
                generationOptions.forEach { option ->
                    val optionKey = option.split("=")[0]
                    val existingOptionIndex = newGenerationOptions.indexOfFirst { it.startsWith(optionKey) }
                    if (existingOptionIndex != -1) {
                        newGenerationOptions[existingOptionIndex] = option
                    } else {
                        newGenerationOptions.add(option)
                    }
                }
                val newProfile = profile.withGenerationOptions(newGenerationOptions.joinToString(separator = " "))
                modifiedProfiles.add(newProfile)
            } else {
                modifiedProfiles.add(profile)
            }
        }
        cmakeSettings.setProfiles(modifiedProfiles)
    }

    private fun removeGenerationOptions(profileName: String?, generationOptions: List<String>) {
        // no need to specify the whole line, with just passing CMAKE_PROJECT_TOP_LEVEL_INCLUDES will remove
        // the whole option
        val cmakeSettings = CMakeSettings.getInstance(project)
        val profiles = cmakeSettings.profiles
        val modifiedProfiles: MutableList<CMakeSettings.Profile> = mutableListOf()

        for (profile in profiles) {
            if (profile.name == profileName) {
                val existingGenerationOptions = profile.generationOptions ?: ""
                val newGenerationOptions = mutableListOf<String>()

                existingGenerationOptions.split(" ").forEach { option ->
                    if (!generationOptions.any { option.contains(it) }) {
                        newGenerationOptions.add(option)
                    }
                }

                val newProfile = profile.withGenerationOptions(newGenerationOptions.joinToString(separator = " "))
                modifiedProfiles.add(newProfile)
            } else {
                modifiedProfiles.add(profile)
            }
        }

        cmakeSettings.setProfiles(modifiedProfiles)
    }

    fun getActiveProfiles(): List<CMakeSettings.Profile> {
        return CMakeSettings.getInstance(project).activeProfiles
    }

    fun getSelectedBuildConfiguration(): CMakeConfiguration? {
        return CMakeAppRunConfiguration.getSelectedBuildAndRunConfigurations(project)?.buildConfiguration
    }

    fun handleAdvancedSettings() {
        val isCMakeParallel = !AdvancedSettings.getBoolean("cmake.reload.profiles.sequentially")

        if (project.service<PropertiesComponent>()
                .getBoolean(PersistentStorageKeys.AUTOMANAGE_CMAKE_ADVANCED_SETTINGS)
        ) {
            if (isCMakeParallel) {
                AdvancedSettings.setBoolean("cmake.reload.profiles.sequentially", true)
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("com.jfrog.conan.plugin.notifications.general")
                    .createNotification(
                        UIBundle.message("cmake.parallel.autoactivated.title"),
                        UIBundle.message("cmake.parallel.autoactivated.body"),
                        NotificationType.INFORMATION
                    )
                    .notify(project)
            }
        } else if (isCMakeParallel && listOf(1).size > 1) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("com.jfrog.conan.plugin.notifications.general")
                .createNotification(
                    UIBundle.message("cmake.parallel.notactivated.title"),
                    UIBundle.message("cmake.parallel.notactivated.body"),
                    NotificationType.WARNING
                )
                .notify(project)
        }
    }
}
