package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.jfrog.conan.clionplugin.conan.ConanPluginUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths
import java.util.*
import javax.swing.event.EventListenerList

// TODO: Figure out why settings (IE, this persitence) is not persisted, but just sometimes!
@Service(Service.Level.PROJECT)
class RemotesDataStateService : PersistentStateComponent<RemotesDataStateService.State> {


    val listeners: EventListenerList = EventListenerList()

    private fun getRemoteStateFilePath(): String {
        return Paths.get(ConanPluginUtils.getPluginHome(),"remote-data.json").toString()
    }

    override fun getState(): State? {
        return state
    }

    @Serializable
    data class State(val conancenter: HashMap<String, HashMap<String, HashMap<String, String>>>)

    private var state: State? = null

    override fun loadState(newState: State) {
        fireStateChangeListener(newState)
        state = newState

        try {
            File(ConanPluginUtils.getPluginHome()).mkdir()
            val path = File(getRemoteStateFilePath())
            val fileCreationResult = path.createNewFile()

            if (!fileCreationResult) {
                thisLogger().warn("Could not create storage file")
            }
            path.writeText(Json.encodeToString(state))
        } catch (e: Exception) {
            thisLogger().error(e.message)
        }

    }

    override fun noStateLoaded() {
        val remoteStateStoreFile = File(getRemoteStateFilePath())

        val initialText = if (remoteStateStoreFile.exists()) {
            remoteStateStoreFile.readText()
        } else {
            javaClass.classLoader.getResource("conan/base-data.json")?.readText()
        }

        if (initialText == null) {
            throw Exception()
        }

        try {
            val newState = Json.decodeFromString<State>(initialText)
            loadState(newState)
        } catch (e: SerializationException) {
            thisLogger().error(e.message)
        }
    }


    interface RemoteDataStateListener : EventListener {
        /**
         * This fine grain notification tells listeners the exact range
         * of cells, rows, or columns that changed.
         *
         * @param `TableModelEvent` to notify listener that a table model
         * has changed
         */
        fun stateChanged(newState: State?)
    }


    fun addStateChangeListener(listener: RemoteDataStateListener) {
        listeners.add(RemoteDataStateListener::class.java, listener)
        if (state != null) {
            listener.stateChanged(state)
        }
    }

    private fun fireStateChangeListener(newState: State) {
        for (listener in listeners.getListeners(RemoteDataStateListener::class.java)) {
            listener.stateChanged(newState)
        }
    }

}
