package com.jfrog.conan.clion.services

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jfrog.conan.clion.cmake.CMake
import com.jfrog.conan.clion.conan.ConanPluginUtils
import com.jfrog.conan.clion.conan.extensions.downloadFromUrl
import com.jfrog.conan.clion.models.LibraryData
import com.jfrog.conan.clion.models.PersistentStorageKeys
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Service(Service.Level.PROJECT)
class ConanService(val project: Project) {

    private val onConfiguredListeners: HashMap<String, (isConfigured: Boolean) -> Unit> = hashMapOf()
    private val onLibraryDataChangeListeners: HashMap<String, (newLibraryData: LibraryData) -> Unit> = hashMapOf()

    fun addOnConfiguredListener(name: String, callback: (isConfigured: Boolean)->Unit) {
        onConfiguredListeners[name] = callback
    }

    fun onWindowReady() {
        fireOnConfiguredListeners(isPluginConfigured())
        fireOnLibraryDataChanged(getRemoteData())
    }

    fun fireOnConfiguredListeners(isConfigured: Boolean) {
        onConfiguredListeners.forEach { it.value(isConfigured) }
    }

    fun addOnLibraryDataChangedListener(name: String, listener: (newTargetData: LibraryData) -> Unit) {
        onLibraryDataChangeListeners[name] = listener
    }

    private fun fireOnLibraryDataChanged(newTargetData: LibraryData) {
        onLibraryDataChangeListeners.forEach{it.value(newTargetData)}
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
            targetFile.parentFile.mkdirs()
            targetFile.downloadFromUrl(cmakeProviderURL)
            // Re-write it, but adding the overwrite header
            ConanPluginUtils.writeToFileWithOverwriteComment(targetFile, targetFile.readText())
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetFile)
        }
    }

    private fun getRemoteDataFilename(): String {
        return "remote-data.json"
    }

    private fun getRemoteDataFile(): File {
        return File(ConanPluginUtils.getPluginHome(), getRemoteDataFilename())
    }

    fun downloadLibraryData(update: Boolean = false) {
        val remoteDataURL = "https://raw.githubusercontent.com/conan-io/conan-clion-plugin/develop2/src/main/resources/conan/targets-data.json"
        val targetFile = getRemoteDataFile()

        if (!targetFile.exists() || update) {
            targetFile.parentFile.mkdirs()
            targetFile.downloadFromUrl(remoteDataURL)
        }
        if (targetFile.exists()) {
            val libraryData = targetFile.readText()

            try {
                val parsedJson = Json{ignoreUnknownKeys=true}.decodeFromString<LibraryData>(libraryData)
                fireOnLibraryDataChanged(parsedJson)
            } catch (e: SerializationException) {
                thisLogger().error(e)
                fireOnLibraryDataChanged(LibraryData(hashMapOf()))
            }
        }
    }

    fun getRemoteData(): LibraryData {
        return try {
            val targetData = getRemoteDataText()
            Json { ignoreUnknownKeys = true }.decodeFromString<LibraryData>(targetData)
        } catch (e: SerializationException) {
            thisLogger().error(e)
            LibraryData(hashMapOf())
        }
    }

    fun getRemoteDataText(): String {
        val userHomeFile = getRemoteDataFile()
        if (!userHomeFile.exists()) {
            val defaultFile = ConanService::class.java.classLoader.getResource("conan/targets-data.json")
            return defaultFile.readText()
        }
        return userHomeFile.readText()
    }

    fun isPluginConfigured(): Boolean {
        val properties = project.service<PropertiesComponent>()
        return properties.isValueSet(PersistentStorageKeys.CONAN_EXECUTABLE) && properties.getValue(PersistentStorageKeys.CONAN_EXECUTABLE)
            ?.isNotEmpty() ?: false
    }
}
