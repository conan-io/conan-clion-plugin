package com.jfrog.conan.clionplugin.conan

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessAdapter
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputType
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys
import java.io.File

class Conan(val project: Project) {

    data class RunOutput(
            val exitCode: Int,
            val stdout: String,
            val stderr: String
    )

    private fun runInBackground(args: List<String>, taskTitle: String, onSuccess: (RunOutput) -> Unit) {
        val task = object : Task.Backgroundable(project, taskTitle, true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                val conanExecutable: String = project.service<PropertiesComponent>().getValue(
                        PersistentStorageKeys.CONAN_EXECUTABLE,
                        "conan"
                )
                val command = listOf(conanExecutable) + args
                thisLogger().info("Running command: $command")

                val commandLine = GeneralCommandLine(command)
                        .withWorkDirectory(File(project.basePath!!))

                val processHandler = CapturingProcessHandler(commandLine)

                var exitCode: Int = Int.MIN_VALUE
                var stdout = ""
                var stderr = ""

                processHandler.addProcessListener(object : CapturingProcessAdapter() {
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {

                        if (outputType == ProcessOutputType.STDOUT) {
                            stdout += event.text
                        } else if (outputType == ProcessOutputType.STDERR) {
                            stderr += event.text
                        }
                        var line = event.text.trim()
                        indicator.text2 = line
                        if (line.startsWith("========")) {
                            indicator.text = line.trim('=').trim()
                        }

                        ProgressManager.checkCanceled()
                        if (indicator.isCanceled) {
                            processHandler.destroyProcess()
                            exitCode = 130
                            return
                        }
                    }

                    override fun processTerminated(event: ProcessEvent) {
                        if (exitCode == Int.MIN_VALUE) {
                            exitCode = event.exitCode
                        }
                    }
                })


                processHandler.runProcess()

                onSuccess(RunOutput(exitCode, stdout, stderr))
            }
        }

        task.queue()
    }

    fun list(pattern: String, onSuccess: (RunOutput) -> Unit) {
        val args = "list $pattern -r conancenter --format=json".split(" ").toList()
        runInBackground(args, "Running Conan List Command", onSuccess)
    }

    fun install(name: String, version: String, onSuccess: (RunOutput) -> Unit) {
        val args = "install --requires=$name/$version -r conancenter --build=missing".split(" ").toList()
        runInBackground(args, "Running Conan Install Command", onSuccess)
    }
}
