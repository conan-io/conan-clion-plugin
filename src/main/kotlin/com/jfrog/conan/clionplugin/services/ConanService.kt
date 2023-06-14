package com.jfrog.conan.clionplugin.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.conan.ConanPluginUtils
import com.jfrog.conan.clionplugin.dialogs.ConanInstallDialogWrapper
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
import java.io.File

@Service(Service.Level.PROJECT)
class ConanService(val project: Project) {

    fun runUseFlow(name: String, version: String) {
        val dialog = ConanInstallDialogWrapper(project)
        if (dialog.showAndGet()) {
            createConanfile()
            addStoredDependency(name, version)
            val conanExecutable: String = project.service<PropertiesComponent>().getValue(
                    PersistentStorageKeys.CONAN_EXECUTABLE,
                    "conan"
            )
            dialog.getSelectedInstallProfiles().forEach { profileName ->
                thisLogger().info("Adding Conan configuration to $profileName")
                CMake(project).addGenerationOptions(profileName,
                                                    listOf("-DCMAKE_PROJECT_TOP_LEVEL_INCLUDES=\"${ConanPluginUtils.getCmakeProviderPath()}\"",
                                                           "-DCONAN_COMMAND=\"${conanExecutable}\"")
                )
            }
        }
    }

    fun createConanfile() {
        val file = File(getCMakeWorkspace().projectPath.toString(), "conanfile.py")
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("""
            import os
            from conan import ConanFile
            
            class ConanApplication(ConanFile):
                package_type = "application"
                settings = "os", "compiler", "build_type", "arch"
                generators = "CMakeDeps", "CMakeToolchain"

                def layout(self):
                    self.folders.source = "."
                    self.folders.build = f"cmake-build-{str(self.settings.build_type).lower()}"
                    self.folders.generators = os.path.join(self.folders.build, "generators")

                def requirements(self):
                    requirements = self.conan_data.get('requirements', [])
                    for requirement in requirements:
                        self.requires(requirement)""".trimIndent()
            )
        }
    }

    private fun getCMakeWorkspace(): CMakeWorkspace {
        return CMakeWorkspace.getInstance(project)
    }

    fun addStoredDependency(name: String, version: String) {
        val dependencyFile = File(getCMakeWorkspace().projectPath.toString(), "conandata.yml")
        var text = if (!dependencyFile.exists()) {
            "requirements:"
        } else {
            dependencyFile.readText()
        }
        val newDependency = "$name/$version"
        if (!text.contains(newDependency)) {
            text += "\n    - \"$newDependency\""
        }
        dependencyFile.writeText(text)
    }
}
