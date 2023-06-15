package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.dialogs.ConanInstallDialogWrapper
import java.io.File

@Service(Service.Level.PROJECT)
class ConanService(val project: Project) {

    fun runUseFlow(name: String, version: String) {
        val installDialog = ConanInstallDialogWrapper(project)
        if (installDialog.showAndGet()) {
            createConanfile()
            addRequirement(name, version)
            val cmake = CMake(project)
            installDialog.getSelectedInstallProfiles().forEach {
                thisLogger().info("Adding Conan configuration to $it")
                cmake.injectDependencyProviderToProfile(it)
            }

            installDialog.getUnselectedInstallProfiles().forEach {
                thisLogger().info("Removing Conan configuration from $it")
                cmake.removeDependencyProviderFromProfile(it)
            }
        }
    }

    fun runRemoveRequirementFlow(name: String, version: String) {
        removeRequirement(name, version)
    }

    private fun createConanfile() {
        val file = File(getCMakeWorkspace().projectPath.toString(), "conanfile.py")
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(
                """
            import os
            from conan import ConanFile
            from conan.tools.cmake import cmake_layout
            
            class ConanApplication(ConanFile):
                package_type = "application"
                settings = "os", "compiler", "build_type", "arch"
                generators = "CMakeDeps", "CMakeToolchain"

                def layout(self):
                    cmake_layout(self)

                def requirements(self):
                    requirements = self.conan_data.get('requirements', [])
                    for requirement in requirements:
                        self.requires(requirement)""".trimIndent()
            )
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
        }
    }

    private fun getCMakeWorkspace(): CMakeWorkspace {
        return CMakeWorkspace.getInstance(project)
    }

    private fun addRequirement(name: String, version: String) {
        val requirements = getRequirements()
        if (!requirements.any { it.startsWith("$name/") }) {
            writeRequirementsFile(listOf("$name/$version", *requirements.toTypedArray()))
        }
    }

    private fun removeRequirement(name: String, version: String) {
        val requirements = getRequirements()
        if (requirements.any { it.startsWith("$name/") }) {
            writeRequirementsFile(requirements.filter { it != "$name/$version" })
        }
    }

    fun getRequirements(): List<String> {
        val file = File(getCMakeWorkspace().projectPath.toString(), "conandata.yml")
        if (!file.exists()) return listOf()
        val requirements = mutableListOf<String>()
        val lines = file.readLines()

        var startReading = false
        for (line in lines) {
            if (line.trim() == "requirements:") {
                startReading = true
                continue
            }

            if (startReading && line.trim().startsWith("-")) {
                requirements.add(line.substringAfter("-").trim().replace("\"", ""))
            }
        }

        return requirements
    }

    private fun writeRequirementsFile(requirements: List<String>) {
        val dependencyFile = File(getCMakeWorkspace().projectPath.toString(), "conandata.yml")
        dependencyFile.createNewFile()
        val text = "requirements:\n" + requirements.joinToString("\n") { "  - \"$it\"" }
        dependencyFile.writeText(text)
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dependencyFile)
    }
}
