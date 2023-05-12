package com.jfrog.conan.clionplugin.conan
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.BackgroundTaskQueue
import java.io.File
import java.lang.ProcessBuilder
import java.util.concurrent.TimeUnit
import com.intellij.openapi.project.Project

class Conan (val project: Project){

    fun list(pattern: String): String {
        val args = "conan list $pattern -r conancenter".split(" ").toList()
        thisLogger().warn("$args")
        return this.project.run { val proc = ProcessBuilder(args)
                .directory(File("."))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            proc.waitFor(7, TimeUnit.SECONDS)
            return proc.inputStream.bufferedReader().readText()
        }
    }
}