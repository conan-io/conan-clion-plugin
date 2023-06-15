package com.jfrog.conan.clionplugin.cmake

import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.cidr.cpp.cmake.model.CMakeConfiguration
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration
import com.jetbrains.rd.util.string.printToString

class CMake(val project: Project) {

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
    fun addGenerationOptions(profileName: String?, generationOptions: List<String>) {
        val cmakeSettings = CMakeSettings.getInstance(project)
        val profiles = cmakeSettings.profiles
        val modifiedProfiles: MutableList<CMakeSettings.Profile> = mutableListOf()

        for (profile in profiles) {
            if (profile.name == profileName) {
                val existingGenerationOptions = profile.generationOptions ?: ""
                val newGenerationOptions = mutableListOf<String>()

                newGenerationOptions.add(existingGenerationOptions)

                generationOptions.forEach() { option ->
                    if (!existingGenerationOptions.contains(option)) {
                        newGenerationOptions.add(option)
                    }
                }
                val newProfile = profile.withGenerationOptions(newGenerationOptions.joinToString(separator=" "))
                modifiedProfiles.add(newProfile)
            } else {
                modifiedProfiles.add(profile)
            }
        }
        cmakeSettings.setProfiles(modifiedProfiles)
    }

    fun removeGenerationOptions(profileName: String?, generationOptions: List<String>) {
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
}