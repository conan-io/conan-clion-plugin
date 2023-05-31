package com.jfrog.conan.clionplugin.conan
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.BackgroundTaskQueue
import java.io.File
import java.lang.ProcessBuilder
import java.util.concurrent.TimeUnit
import com.intellij.openapi.project.Project

class Conan (val project: Project) {

    private fun run(args: List<String>): String {
        val conanExecutable: String = this.project.service<PropertiesComponent>().getValue("com.jfrog.conanplugin.conanexecutable", "conan")
        val command = listOf(conanExecutable) + args
        val proc = ProcessBuilder(command)
            .directory(File("."))
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        val output = proc.waitFor()
        return proc.inputStream.bufferedReader().readText()
    }

    fun list(pattern: String): String {
        val args = "list $pattern -r conancenter".split(" ").toList()
        return run(args)
    }

    fun install(name: String, version: String): String {
        val args = "install $name/$version -r conancenter".split(" ").toList()
        return run(args)
    }
}