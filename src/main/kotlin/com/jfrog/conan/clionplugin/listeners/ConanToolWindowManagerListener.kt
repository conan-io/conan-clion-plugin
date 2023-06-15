package com.jfrog.conan.clionplugin.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.jfrog.conan.clionplugin.services.RemotesDataStateService


internal class ConanToolWindowManagerListener(private val project: Project) : ToolWindowManagerListener {

    override fun toolWindowRegistered(id: String) {
        if (id == "Conan") {
            // Things to do when the plugin first loads.
            // As we have not been able to get the remote data state service to realibly sotre its json,
            // we fire the noStateLoaded function manually (As it does not actually implement the @State decorator!)
            this.project.service<RemotesDataStateService>().noStateLoaded()
        }
    }
}
