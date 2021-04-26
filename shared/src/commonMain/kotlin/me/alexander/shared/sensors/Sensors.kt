package me.alexander.shared.sensors

import com.arkivanov.decompose.value.Value

interface Sensors {
    val models: Value<Model>

    data class Model(
        val items: List<Sensor>
    )
}
