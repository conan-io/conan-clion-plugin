package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.jfrog.conan.clionplugin.MyBundle
import com.jfrog.conan.clionplugin.conan.Conan

@Service(Service.Level.PROJECT)
class ConanTaskQueueService(project: Project) {
    private val queue : BackgroundTaskQueue = BackgroundTaskQueue(project, "Conan Tasks")

    fun run(task: Task.Backgroundable) = queue.run(task)
    val isEmpty: Boolean get() = queue.isEmpty

    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }


}