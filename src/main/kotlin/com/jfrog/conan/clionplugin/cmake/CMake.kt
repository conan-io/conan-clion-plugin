package com.jfrog.conan.clionplugin.cmake

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.cidr.cpp.cmake.model.CMakeConfiguration
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration
import com.jfrog.conan.clionplugin.conan.ConanPluginUtils
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys

class CMake(val project: Project) {

    fun checkConanUsedInAnyActiveProfile(): Boolean {
        getActiveProfiles().forEach() { profile ->
            if (checkConanUsedInProfile(profile.name)) {
                return true
            }
        }
        return false
    }

    fun checkConanUsedInProfile(profileName: String?): Boolean {
        val cmakeSettings = CMakeSettings.getInstance(project)
        val profiles = cmakeSettings.profiles
        val modifiedProfiles: MutableList<CMakeSettings.Profile> = mutableListOf()

        for (profile in profiles) {
            if (profile.name == profileName) {
                val existingGenerationOptions = profile.generationOptions ?: ""
                return listOf("CONAN_COMMAND", "conan_provider.cmake").any { existingGenerationOptions.contains(it) }
            } else {
                modifiedProfiles.add(profile)
            }
        }

        cmakeSettings.setProfiles(modifiedProfiles)
        return true
    }

    fun injectDependencyProviderToProfile(profileName: String) {
        val conanExecutable: String = project.service<PropertiesComponent>().getValue(
            PersistentStorageKeys.CONAN_EXECUTABLE,
            ""
        )
        val generationOptions: MutableList<String> = mutableListOf()
        generationOptions.add("-DCMAKE_PROJECT_TOP_LEVEL_INCLUDES=\"${ConanPluginUtils.getCmakeProviderPath()}\"")
        if (conanExecutable != "" && conanExecutable != "conan") {
            generationOptions.add("-DCONAN_COMMAND=\"${conanExecutable}\"")
        }
        addGenerationOptions(profileName, generationOptions)
    }

    fun removeDependencyProviderFromProfile(profileName: String) {
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
                val newGenerationOptions = mutableListOf<String>()

                newGenerationOptions.add(existingGenerationOptions)

                generationOptions.forEach { option ->
                    if (!existingGenerationOptions.contains(option)) {
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

    fun checkConanExecutable(): Boolean {
        val exeConfigured = (project.service<PropertiesComponent>().getValue(PersistentStorageKeys.CONAN_EXECUTABLE, "") != "")
        if (!exeConfigured) {
            // TODO: still missing implementing the 'Use conan in system path' checkbox
            Messages.showMessageDialog("Looks like you have not configured the path to the Conan executable," +
                    " if you want to use the system one please check the 'Use conan in system path' " +
                    "option in the configuration window.","Conan executable path not configured", Messages.getWarningIcon())
        }
        return exeConfigured
    }

    fun addConanSupport() {
        val profiles: MutableList<String> = mutableListOf()
        if (checkConanExecutable()) {
            getActiveProfiles().forEach { profile ->
                thisLogger().info("Adding Conan configuration to ${profile.name}")
                injectDependencyProviderToProfile(profile.name)
                profiles.add(profile.name)
            }
            project.service<PropertiesComponent>().setValue(PersistentStorageKeys.AUTOMATIC_ADD_CONAN, true)
            Messages.showMessageDialog("Conan support added for: ${profiles.joinToString(separator = ", ")}","Conan support added", Messages.getInformationIcon())
        }
    }

    fun getActiveProfiles(): List<CMakeSettings.Profile> {
        return CMakeSettings.getInstance(project).activeProfiles
    }

    fun getSelectedBuildConfiguration(): CMakeConfiguration? {
        return CMakeAppRunConfiguration.getSelectedBuildAndRunConfigurations(project)?.buildConfiguration
    }
}