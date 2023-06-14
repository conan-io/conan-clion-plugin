package com.jfrog.conan.clionplugin.conan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import java.nio.file.Paths

object ConanPluginUtils {
    private const val cmakeProviderFileName = "conan_provider.cmake"
    private const val cmakeProviderURL = "https://raw.githubusercontent.com/conan-io/cmake-conan/develop2/conan_provider.cmake"

    fun getPluginHome(): String {
        return Paths.get(System.getProperty("user.home"), ".conan-clion-plugin").toString()
    }

    fun downloadCMakeProvider(update: Boolean = false) {
        val pluginHome = getPluginHome()
        val targetFile = File(pluginHome, cmakeProviderFileName)

        if (update || !targetFile.exists()) {
            targetFile.parentFile.mkdirs()
            targetFile.downloadFromUrl(cmakeProviderURL)
        }
    }

    private fun File.downloadFromUrl(url: String) {
        val targetFile = this

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val fileUrl = URL(url)
                fileUrl.openStream().use { input ->
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
