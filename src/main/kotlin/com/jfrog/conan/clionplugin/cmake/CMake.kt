package com.jfrog.conan.clionplugin.cmake

import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.cidr.cpp.cmake.model.CMakeConfiguration
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration
import com.jetbrains.rd.util.string.printToString

class CMake(val project: Project) {
    public fun addGenerationOptions(profileName: String?, generationOptions: String) {
        val cmakeSettings = CMakeSettings.getInstance(project)
        val profiles = cmakeSettings.profiles
        val modifiedProfiles: MutableList<CMakeSettings.Profile> = mutableListOf()

        for (profile in profiles) {
            println(profile.printToString())
            if (profile.name == profileName) {
                val newProfile = profile.withGenerationOptions(generationOptions)
                modifiedProfiles.add(newProfile)
            }
            else {
                modifiedProfiles.add(profile)
            }
        }
        cmakeSettings.setProfiles(modifiedProfiles)
    }

    public fun getActiveProfiles(): List<CMakeSettings.Profile> {
        return CMakeSettings.getInstance(project).activeProfiles
    }

    public fun getSelectedBuildConfiguration(): CMakeConfiguration? {
        return CMakeAppRunConfiguration.getSelectedBuildAndRunConfigurations(project)?.buildConfiguration
    }
}