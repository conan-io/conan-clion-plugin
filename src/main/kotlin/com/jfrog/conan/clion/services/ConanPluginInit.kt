package com.jfrog.conan.clion.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.startup.StartupActivity


class ConanPluginInit : ProjectActivity {

    override suspend fun execute(project: Project) {
        project.service<ConanService>().downloadLibraryData()
    }
}
