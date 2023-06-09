package com.jfrog.conan.clionplugin.conan.extensions


import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.CMakeRunnerStep
import com.jetbrains.cidr.cpp.cmake.CMakeRunnerStep.Parameters
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.rd.util.string.printToString

class ConanCMakeRunnerStep : CMakeRunnerStep {
    override fun beforeGeneration(project: Project, parameters: Parameters) {
        val profileName = parameters.getUserData(Parameters.PROFILE_NAME)
        val cmakeSettings = CMakeSettings.getInstance(project)
        val profile = cmakeSettings.profiles.find { it.name == profileName }
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
