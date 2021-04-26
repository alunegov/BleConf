package me.alexander.shared.sensors.store

import com.arkivanov.mvikotlin.core.store.Store
import me.alexander.shared.sensors.Sensor
import me.alexander.shared.sensors.store.SensorsStore.Intent
import me.alexander.shared.sensors.store.SensorsStore.State

internal interface SensorsStore : Store<Intent, State, Nothing> {

    sealed class Intent {
        object First: Intent()
    }

    data class State(
        val items: List<Sensor> = emptyList(),
    )
}
