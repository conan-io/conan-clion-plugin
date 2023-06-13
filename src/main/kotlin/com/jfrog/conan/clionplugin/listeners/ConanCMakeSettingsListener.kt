package com.jfrog.conan.clionplugin.listeners

import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.cidr.cpp.cmake.CMakeSettingsListener
import com.jetbrains.cidr.cpp.cmake.model.CMakeListener


internal class ConanCMakeSettingsListener(private val project: Project) : CMakeSettingsListener {
    override fun autoReloadChanged() {
        println("autoReloadChanged")
    }

    override fun profilesChanged(old: List<CMakeSettings.Profile>, current: List<CMakeSettings.Profile>) {
        println("profilesChanged")
    }
}
