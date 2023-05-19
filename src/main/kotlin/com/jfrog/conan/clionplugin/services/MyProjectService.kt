package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.util.ui.table.TableModelEditor.DataChangedListener
import com.jfrog.conan.clionplugin.MyBundle
import com.jfrog.conan.clionplugin.conan.Conan
import com.jfrog.conan.clionplugin.conan.datamodels.Package
import com.jfrog.conan.clionplugin.conan.datamodels.Recipe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlin.properties.Delegates

@Service(Service.Level.PROJECT)
class MyProjectService(val project: Project) {

    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    val refreshListListeners = ArrayList<(ConanPackagesDao) -> Unit>()

    // fires off every time value of the property changes
    var packages: ConanPackagesDao by Delegates.observable(ConanPackagesDao(hashMapOf())) { property, oldValue, newValue: ConanPackagesDao ->
        // do your stuff here
        refreshListListeners.forEach {
            it(newValue)
        }
    }

    @Serializable
    data class ConanPackagesDao(val conancenter: HashMap<String, Recipe>)
    fun getConanPackages(searchPattern: String): ConanPackagesDao {
        val output: String = Conan(project).list("$searchPattern -r=conancenter --format=json")
        return Json.decodeFromString<ConanPackagesDao>(output)
    }

    fun listConanPackages() {
        val output = Conan(project).list("* -r=conancenter --format=json")
        packages = Json.decodeFromString<ConanPackagesDao>(output)
    }

}
