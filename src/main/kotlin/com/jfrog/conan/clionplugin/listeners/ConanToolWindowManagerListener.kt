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
            // Things to do
            this.project.service<RemotesDataStateService>().noStateLoaded()
        }
    }
}
