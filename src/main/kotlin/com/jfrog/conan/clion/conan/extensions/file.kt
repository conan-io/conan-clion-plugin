package com.jfrog.conan.clion.conan.extensions

import com.intellij.openapi.diagnostic.thisLogger
import java.io.File
import java.net.URI

fun File.downloadFromUrl(url: String) {
    val targetFile = this

    try {
        val fileUrl = URI(url).toURL()
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
