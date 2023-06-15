package com.jfrog.conan.clionplugin.conan

import com.jfrog.conan.clionplugin.conan.extensions.downloadFromUrl
import java.io.File
import java.nio.file.Paths

object ConanPluginUtils {
    private const val cmakeProviderFileName = "conan_provider.cmake"
    private const val cmakeProviderURL = "https://raw.githubusercontent.com/conan-io/cmake-conan/develop2/conan_provider.cmake"

    fun getPluginHome(): String {
        return Paths.get(System.getProperty("user.home"), ".conan-clion-plugin").toString()
    }

    fun getCmakeProviderPath(): String {
        return "${getPluginHome()}/${cmakeProviderFileName}"
    }

    fun downloadCMakeProvider(update: Boolean = false) {
        val pluginHome = getPluginHome()
        val targetFile = File(pluginHome, cmakeProviderFileName)

        if (update || !targetFile.exists()) {
            targetFile.parentFile.mkdirs()
            targetFile.downloadFromUrl(cmakeProviderURL)
        }
    }
}
