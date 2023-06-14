package com.jfrog.conan.clionplugin.conan

import java.nio.file.Paths

class ConanPluginInfo {
    companion object {
        val cmakeProviderFileName = "conan_provider.cmake"
        val cmakeProviderURL = "https://raw.githubusercontent.com/conan-io/cmake-conan/develop2/conan_provider.cmake"

        fun getPluginHome(): String {
            return  Paths.get(System.getProperty("user.home"), ".conan-clion-plugin").toString()
        }
    }
}
