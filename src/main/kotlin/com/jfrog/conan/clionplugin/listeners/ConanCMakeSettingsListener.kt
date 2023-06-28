package com.jfrog.conan.clionplugin.listeners

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.cidr.cpp.cmake.CMakeSettingsListener
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys


internal class ConanCMakeSettingsListener(private val project: Project) : CMakeSettingsListener {
    override fun autoReloadChanged() {
        println("autoReloadChanged")
    }

    override fun profilesChanged(old: List<CMakeSettings.Profile>, current: List<CMakeSettings.Profile>) {
        val properties = project.service<PropertiesComponent>()
        val autoAdd = (properties.getValue(PersistentStorageKeys.AUTOMATIC_ADD_CONAN, "false") == "true")
        if (autoAdd) {
            // get only the new profiles
            current
                .filter { new -> old.any { new.name == it.name }}
                .forEach { CMake(project).injectDependencyProviderToProfile(it.name) }
        }
    }
}
