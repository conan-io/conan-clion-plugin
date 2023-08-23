package com.jfrog.conan.clion.conan.extensions

import com.intellij.openapi.diagnostic.thisLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL

suspend fun File.downloadFromUrl(url: String) {
    val targetFile = this

    try {
        val fileUrl = URL(url)
        fileUrl.openStream().use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        thisLogger().info("File downloaded: ${targetFile.absolutePath}")
    } catch (e: Exception) {
        thisLogger().warn("Error downloading: ${e.message}")
    }
}
