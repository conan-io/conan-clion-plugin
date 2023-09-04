package com.jfrog.conan.clion.listeners

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.cidr.cpp.cmake.CMakeSettingsListener
import com.jfrog.conan.clion.cmake.CMake
import com.jfrog.conan.clion.models.PersistentStorageKeys


internal class ConanCMakeSettingsListener(private val project: Project) : CMakeSettingsListener {
    override fun profilesChanged(old: List<CMakeSettings.Profile>, current: List<CMakeSettings.Profile>) {
        val properties = project.service<PropertiesComponent>()
        val autoAdd = (properties.getValue(PersistentStorageKeys.AUTOMATIC_ADD_CONAN, "false") == "true")
        if (autoAdd) {
            // Add only to new profiles, if old profiles don't have
            // Conan support is because it was manually disabled
            current
                .filter { new -> old.none { new.name == it.name }}
                .forEach { CMake(project).injectDependencyProviderToProfile(it.name) }
        }
    }
}
