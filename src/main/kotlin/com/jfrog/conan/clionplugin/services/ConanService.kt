package com.jfrog.conan.clionplugin.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration
import com.jetbrains.rd.util.string.printToString
import com.jfrog.conan.clionplugin.cmake.CMake
import com.jfrog.conan.clionplugin.conan.Conan
import com.jfrog.conan.clionplugin.dialogs.ConanInstallDialogWrapper
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Service(Service.Level.PROJECT)
class ConanService(val project: Project) {

    public fun runInstallFlow(name: String, version: String) {
        val selectedRunConfig = CMakeAppRunConfiguration.getSelectedRunConfiguration(project)
        //println(selectedRunConfig.printToString())
        val selectedBuildConfig = CMakeAppRunConfiguration.getSelectedBuildAndRunConfigurations(project)?.buildConfiguration
        //println(selectedBuildConfig.printToString())
        val cmakeSettings = CMakeSettings.getInstance(project)
        val activeProfiles = cmakeSettings.activeProfiles
        val selectedProfile = activeProfiles.find { it.name == selectedBuildConfig?.profileName }
        println(selectedProfile.printToString())
        println(selectedBuildConfig?.buildWorkingDir)
        println(selectedBuildConfig?.configurationAndTargetGenerationDir)
        println(selectedBuildConfig?.configurationGenerationDir)

        val conan = Conan(project)
        val dialog = ConanInstallDialogWrapper(project)
        if (dialog.showAndGet()) {
            addStoredDependency(name, version)
            createConanfile()
            dialog.getSelectedInstallProfiles().forEach { profileName ->
                // TODO: Pass extra info to conan install for each one of the it profiles
                thisLogger().info("Installing for $profileName")
                val buildType = activeProfiles.find { it.name == profileName }?.buildType
                conan.install(name, version, buildType) { runOutput ->
                    thisLogger().info("Command exited with status ${runOutput.exitCode}")
                    thisLogger().info("Command stdout: ${runOutput.stdout}")
                    thisLogger().info("Command stderr: ${runOutput.stderr}")
                    var message = ""
                    if (runOutput.exitCode != 130) {
                        message = "$name/$version installed successfully"
                        CMake(project).addGenerationOptions(
                            selectedBuildConfig?.profileName,
                            "-DCMAKE_TOOLCHAIN_FILE=\"${selectedBuildConfig?.configurationAndTargetGenerationDir}/generators/conan_toolchain.cmake\""
                        )
                    } else {
                        message = "Conan process canceled by user"
                    }
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Conan Notifications Group")
                        .createNotification(
                            message,
                            runOutput.stdout,
                            NotificationType.INFORMATION
                        )
                        .notify(project);
                }

            }
        }
    }

    fun createConanfile() {
        val workspace = getCMakeWorkspace()
        val file = File(workspace.projectPath.toString(), "conanfile.py")
        file.createNewFile()
        val requirements = getStoredDependencies().joinToString("\n") {
            "        self.requires('$it')"
        }
        file.writeText("""
import os
from conan import ConanFile


class ConanApplication(ConanFile):
    package_type = "application"
    settings = "os", "compiler", "build_type", "arch"
    generators = "CMakeDeps", "CMakeToolchain"

    def layout(self):
        # consider other generators? Clion uses Ninja by default
        self.conf.define("tools.cmake.cmaketoolchain:generator", "Ninja")
        self.folders.source = "."
        self.folders.build = f"cmake-build-{str(self.settings.build_type).lower()}"
        self.folders.generators = os.path.join(self.folders.build, "generators")

    def requirements(self):
        # add requirements dinamically, from a json file stored in the user's path?  
$requirements
""")
    }


    private fun getCMakeWorkspace(): CMakeWorkspace {
        return CMakeWorkspace.getInstance(project)
    }



    fun addStoredDependency(name: String, version: String) {
        val dependencies = mutableListOf("$name/$version")
        dependencies.addAll(getStoredDependencies())
        val dependencyFile = File(getCMakeWorkspace().projectPath.toString(), "conan.json")
        dependencyFile.writeText(Json.encodeToString(dependencies.toSet()))
    }

    private fun getStoredDependencies(): List<String> {
        val dependencyFile = File(getCMakeWorkspace().projectPath.toString(), "conan.json")
        if (dependencyFile.exists()) {
            return Json.decodeFromString<List<String>>(dependencyFile.readText())
        }
        return listOf()
    }
}