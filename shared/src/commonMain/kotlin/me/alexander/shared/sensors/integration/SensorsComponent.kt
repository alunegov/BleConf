package me.alexander.shared.sensors.integration

import com.juul.kable.Peripheral
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.store.StoreFactory
import me.alexander.shared.sensors.Sensors
import me.alexander.shared.sensors.Sensors.Model
import me.alexander.shared.sensors.store.SensorsStore
import me.alexander.shared.sensors.store.SensorsStore.State
import me.alexander.shared.sensors.store.SensorsStoreProvider
import me.alexander.shared.utils.asValue

class SensorsComponent(
    val storeFactory: StoreFactory,
    val periph: Peripheral,
) : Sensors {

    private val store = SensorsStoreProvider(
        storeFactory = storeFactory,
        periph = periph,
    ).provide()

    private val stateToModel: (State) -> Model = {
        Model(
            items = it.items,
        )
    }

    override val models: Value<Model> = store.asValue().map(stateToModel)

    fun refresh() {
        store.accept(SensorsStore.Intent.Refresh)
    }

    fun toggleEnable(id: String) {
        store.accept(SensorsStore.Intent.ToggleEnable(id))
    }
}
