package com.jfrog.conan.clionplugin.listeners

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.jfrog.conan.clionplugin.services.RemotesDataStateService
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


internal class ConanToolWindowManagerListener(private val project: Project) : ToolWindowManagerListener {

    override fun toolWindowRegistered(id: String) {
        if (id == "Conan") {
            val remoteDataService = this.project.service<RemotesDataStateService>()
            if (remoteDataService.state == null) {
                val baseContent = javaClass.classLoader.getResource("conan/base-data.json")?.readText()
                if (baseContent != null) {
                    try {
                        val newState = Json.decodeFromString<RemotesDataStateService.State>(baseContent)
                        remoteDataService.loadState(newState)
                    } catch (e: SerializationException) {
                        thisLogger().error(e.message)
                    }

                }

            }
        }
    }
}
