package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.jfrog.conan.clionplugin.conan.ConanPluginUtils


class ConanPluginInit : ProjectActivity {
    override suspend fun execute(project: Project) {
        ConanPluginUtils.downloadCMakeProvider()
    }
}
