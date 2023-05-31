package com.jfrog.conan.clionplugin.conan
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jfrog.conan.clionplugin.models.PersistentStorageKeys

class Conan (val project: Project) {

    private fun run(args: List<String>): String {
        val conanExecutable: String = this.project.service<PropertiesComponent>().getValue(PersistentStorageKeys.CONAN_EXECUTABLE, "conan")
        val handler = ScriptRunnerUtil.execute(conanExecutable, project.basePath, null, args.toTypedArray())
        return ScriptRunnerUtil.getProcessOutput(handler, ScriptRunnerUtil.STDOUT_OUTPUT_KEY_FILTER, 1000 * 1000)
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