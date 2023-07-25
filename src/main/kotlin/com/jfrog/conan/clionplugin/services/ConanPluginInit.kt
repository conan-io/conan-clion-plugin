package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity


class ConanPluginInit : StartupActivity {
    override fun runActivity(project: Project) {
        project.service<ConanService>().downloadLibraryData()
    }
}
