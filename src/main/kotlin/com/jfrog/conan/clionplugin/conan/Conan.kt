package com.jfrog.conan.clionplugin.conan

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
import java.io.File

class Conan(val project: Project) {

    data class RunOutout (
        val exitCode: Int,
        val stdout: String,
        val stderr: String
    )

    private fun run(args: List<String>): RunOutout {
        val conanExecutable: String = this.project.service<PropertiesComponent>().getValue(PersistentStorageKeys.CONAN_EXECUTABLE, "conan")
        val command = listOf(conanExecutable) + args
        thisLogger().info("Running command: $command")

        val process = ProcessBuilder(command)
                .directory(File(project.basePath!!))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        val stdout = process.inputStream.bufferedReader().use { it.readText() }
        val stderr = process.errorStream.bufferedReader().use { it.readText() }

        val exitCode = process.waitFor()

        thisLogger().info("Command exited with status $exitCode")
        thisLogger().info("Command stdout: $stdout")
        thisLogger().warn("Command stderr: $stderr")

        return RunOutout(exitCode, stdout, stderr)
    }

    fun list(pattern: String): RunOutout {
        val args = "list $pattern -r conancenter --format=json".split(" ").toList()
        return run(args)
    }

    fun install(name: String, version: String): RunOutout {
        val args = "install --requires=$name/$version -r=conancenter --build=missing".split(" ").toList()
        return run(args)
    }
}
