package com.jfrog.conan.clionplugin.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import kotlinx.serialization.Serializable
import java.util.*
import javax.swing.event.EventListenerList
import javax.swing.event.TableModelEvent

@Service(Service.Level.PROJECT)
@State(name = "remotes-data", storages = [Storage("conanPlugin.xml")])
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
    }


    interface RemoteDataStateListener : EventListener {
        /**
         * This fine grain notification tells listeners the exact range
         * of cells, rows, or columns that changed.
         *
         * @param e a `TableModelEvent` to notify listener that a table model
         * has changed
         */
        fun stateChanged(newState: State)
    }


    fun addStateChangeListener(listener: RemoteDataStateListener) {
        listeners.add(RemoteDataStateListener::class.java, listener)
    }

    private fun fireStateChangeListener(newState: State) {
        for (listener in listeners.getListeners(RemoteDataStateListener::class.java)) {
            listener.stateChanged(newState)
        }
    }

}
