package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.application.Application
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.ThreeState
import com.intellij.util.application
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*
import javax.swing.event.EventListenerList
import javax.swing.event.TableModelEvent

// TODO: Figure out why settings (IE, this persitence) is not persisted, but just sometimes!
@Service(Service.Level.PROJECT)
@State(name = "remotes-data", storages = [Storage("conanPlugin.xml", useSaveThreshold = ThreeState.NO)])
class RemotesDataStateService : PersistentStateComponent<RemotesDataStateService.State> {


    val listeners: EventListenerList = EventListenerList()


    override fun getState(): State? {
        return state
    }

    @Serializable
    data class State(val conancenter: HashMap<String, HashMap<String, HashMap<String, String>>>)

    private var state: State? = null

    override fun loadState(newState: State) {
        fireStateChangeListener(newState)
        state = newState
        application.saveSettings()
    }

    override fun noStateLoaded() {
        val baseContent = javaClass.classLoader.getResource("conan/base-data.json")?.readText()
        if (baseContent != null) {
            try {
                val newState = Json.decodeFromString<State>(baseContent)
                loadState(newState)
            } catch (e: SerializationException) {
                thisLogger().error(e.message)
            }
        }
    }


    interface RemoteDataStateListener : EventListener {
        /**
         * This fine grain notification tells listeners the exact range
         * of cells, rows, or columns that changed.
         *
         * @param e a `TableModelEvent` to notify listener that a table model
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
