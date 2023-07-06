package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity


class ConanPluginInit : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Do nothing
    }
}
