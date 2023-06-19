package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.jfrog.conan.clionplugin.conan.ConanPluginUtils
import com.intellij.openapi.options.advanced.AdvancedSettings


class ConanPluginInit : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<ConanService>().downloadCMakeProvider()
    }
}
