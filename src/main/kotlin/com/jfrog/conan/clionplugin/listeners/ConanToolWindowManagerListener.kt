package com.jfrog.conan.clionplugin.listeners

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.jfrog.conan.clionplugin.services.MyProjectService


internal class ConanToolWindowManagerListener(private val project: Project) : ToolWindowManagerListener {

    override fun toolWindowRegistered(id: String) {
        if (id == "Conan") {
            this.project.service<MyProjectService>().listConanPackages()
        }
    }
}
