package com.jfrog.conan.clion

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jfrog.conan.clion.services.ConanService

class MyPluginTest : BasePlatformTestCase() {

    fun testProjectService() {
        val projectService = project.service<ConanService>()
    }

    override fun getTestDataPath() = "src/test/testData/rename"
}
