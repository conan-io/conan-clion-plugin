package com.jfrog.conan.plugin.conan

import com.intellij.openapi.project.Project
import com.jfrog.conan.plugin.models.LibraryData
import java.io.File
import java.nio.file.Paths

object ConanPluginUtils {
    fun getPluginHome(): String {
        return Paths.get(System.getProperty("user.home"), ".conan-clion-plugin").toString()
    }


    private val OVERWRITE_HEADER = "# This file is managed by Conan, contents will be overwritten.\n" +
            "# To keep your changes, remove these comment lines, but the plugin won't be able to modify your requirements\n"

    fun fileHasOverwriteComment(file: File): Boolean {
        if (!file.exists()) return true
        val text = file.readText()
        val startsWith = text.startsWith(OVERWRITE_HEADER)
        return startsWith
    }

    fun writeToFileWithOverwriteComment(file: File, content: String) {
        file.writeText("$OVERWRITE_HEADER\n$content")
    }


}
