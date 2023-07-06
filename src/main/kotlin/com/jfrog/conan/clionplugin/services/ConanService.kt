package com.jfrog.conan.clionplugin.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.conan.ConanPluginUtils
import com.jfrog.conan.clionplugin.conan.extensions.downloadFromUrl
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
import kotlinx.coroutines.runBlocking
import java.io.File

@Service(Service.Level.PROJECT)
class ConanService(val project: Project) {

    private val onConfiguredListeners: HashMap<String, (isConfigured: Boolean) -> Unit> = hashMapOf()

    init {

    }

    fun addOnConfiguredListener(name: String, callback: (isConfigured: Boolean) -> Unit) {
        onConfiguredListeners[name] = callback
    }

    fun fireOnConfiguredListeners(isConfigured: Boolean) {
        onConfiguredListeners.forEach { it.value(isConfigured) }
    }

    fun runUseFlow(name: String, version: String) {
        val cmake = CMake(project)
        // TODO: Launch a warning also if we have not configured a path for the Conan executable?
        //       In that case we could check with a conan --version if Conan is in the path
        //       or maybe doing that on startup
        if (!cmake.checkConanUsedInAnyActiveProfile()) {
            Messages.showMessageDialog(
                "Looks like Conan support may have not been added to the project. \n" +
                        "Please click on the add button to add Conan support", "Add Conan support to the project",
                Messages.getWarningIcon()
            )
        }
        createConanfile()
        addRequirement(name, version)
    }

    fun runRemoveRequirementFlow(name: String, version: String) {
        removeRequirement(name, version)
    }

    private fun createConanfile() {
        val file = File(getCMakeWorkspace().projectPath.toString(), "conanfile.py")
        if (ConanPluginUtils.fileHasOverwriteComment(file)) {
            file.createNewFile()
            ConanPluginUtils.writeToFileWithOverwriteComment(
                file, """
                from conan import ConanFile
                from conan.tools.cmake import cmake_layout, CMakeToolchain
                
                class ConanApplication(ConanFile):
                    package_type = "application"
                    settings = "os", "compiler", "build_type", "arch"
                    generators = "CMakeDeps"

                    def layout(self):
                        cmake_layout(self)

                    def generate(self):
                        tc = CMakeToolchain(self)
                        tc.user_presets_path = False
                        tc.generate()

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
        if (ConanPluginUtils.fileHasOverwriteComment(dependencyFile)) {
            dependencyFile.createNewFile()
            val text = "requirements:\n" + requirements.joinToString("\n") { "  - \"$it\"" }
            ConanPluginUtils.writeToFileWithOverwriteComment(dependencyFile, text)
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dependencyFile)
        }
    }

    private fun getCMakeProviderFilename(): String {
        return "conan_provider.cmake"
    }

    fun getCMakeProviderFile(): File {
        return File(CMakeWorkspace.getInstance(project).projectPath.toString(), getCMakeProviderFilename())
    }

    fun downloadCMakeProvider(update: Boolean = false) {
        val cmakeProviderURL = "https://raw.githubusercontent.com/conan-io/cmake-conan/develop2/conan_provider.cmake"
        val targetFile = getCMakeProviderFile()

        if (!targetFile.exists() || update && ConanPluginUtils.fileHasOverwriteComment(targetFile)) {
            val tempTargetFile = File(ConanPluginUtils.getPluginHome(), getCMakeProviderFilename())
            tempTargetFile.parentFile.mkdirs()
            runBlocking {
                tempTargetFile.downloadFromUrl(cmakeProviderURL)
            }


            val originalText = tempTargetFile.readText()
            targetFile.parentFile.mkdirs()

            ConanPluginUtils.writeToFileWithOverwriteComment(targetFile, originalText)
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetFile)
        }
    }

    fun isPluginConfigured(): Boolean {
        val properties = project.service<PropertiesComponent>()
        return properties.isValueSet(PersistentStorageKeys.CONAN_EXECUTABLE) && properties.getValue(
            PersistentStorageKeys.CONAN_EXECUTABLE
        )
            ?.isNotEmpty() ?: false
    }
}
