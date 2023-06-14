package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.jfrog.conan.clionplugin.conan.ConanPluginInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL

class ConanPluginInit : ProjectActivity {

    override suspend fun execute(project: Project) {
        val pluginHome = ConanPluginInfo.getPluginHome()
        val targetFile = File(pluginHome, ConanPluginInfo.cmakeProviderFileName)

        if (!targetFile.exists()) {
            val fileUrl = ConanPluginInfo.cmakeProviderURL
            downloadFile(fileUrl, targetFile)
        }
    }

    private fun downloadFile(fileUrl: String, targetFile: File) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(fileUrl)
                url.openStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                println("Conan CMake provider downloaded: ${targetFile.absolutePath}")
            } catch (e: Exception) {
                println("Error downloading: ${e.message}")
            }
        }
    }
}
