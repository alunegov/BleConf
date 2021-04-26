package me.alexander.shared.sensors.integration

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.alexander.shared.sensors.Sensors
import me.alexander.shared.sensors.Sensors.Model
import me.alexander.shared.sensors.store.SensorsStoreProvider

class SensorsComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory
) : Sensors, ComponentContext by componentContext {

    private val store = SensorsStoreProvider(
        storeFactory = storeFactory
    ).provide()

    override val models: Value<Model> = MutableValue(Model(emptyList()))// = store.asValue().map(it -> Model(it.values))
}
