package me.alexander.shared.sensors.store

import kotlinx.coroutines.flow.Flow
import com.arkivanov.mvikotlin.core.store.*
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.benasher44.uuid.uuidFrom
import com.juul.kable.characteristicOf
import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import me.alexander.shared.sensors.Sensor
import me.alexander.shared.sensors.store.SensorsStore.Intent
import me.alexander.shared.sensors.store.SensorsStore.State

internal class SensorsStoreProvider(
    private val storeFactory: StoreFactory,
    private val periph: Peripheral,
) {
    fun provide(): SensorsStore =
        object : SensorsStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "SensorsStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed class Result {
        data class Loaded(val items: List<Sensor>) : Result()
        data class EnableToggled(val id: String) : Result()
        data class StateChanged(val id: String, val state: Int) : Result()
        data class CoeffChanged(val id: String, val coeff: Double) : Result()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {
        override suspend fun executeAction(action: Unit, getState: () -> State) {
            refresh()

            //val gg: Flow<ByteArray> = periph.observe(characteristicOf("", ""))
            //gg.collect { accept(Intent.ChangeState("", 0)) }
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                Intent.Refresh -> refresh()
                is Intent.ToggleEnable -> toggleEnable(intent.id, getState)
            }
        }

        private val ENABILITY_SERVIVE_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f07"
        private val ENABLED_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f17"
        private val UPDATES_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f27"

        private val STATES_SERVIVE_UUID = "4834cad6-5043-4ed9-8d85-a277e72c8178"

        private suspend fun refresh() {
            val items: MutableList<Sensor> = emptyList<Sensor>().toMutableList()

            periph.services?.firstOrNull { it.serviceUuid == uuidFrom(STATES_SERVIVE_UUID) }?.characteristics?.forEach {
                val state = periph.read(it)
                items += Sensor(it.characteristicUuid.toString(), state[0], false, null)
            }

            val enabled = periph.read(characteristicOf(ENABILITY_SERVIVE_UUID, ENABLED_CH_UUID))
            //items[0] = items[0].copy(enabled = (enabled[0].toInt() or (1 shl 0)) != 0)

            dispatch(Result.Loaded(items))
        }

        private suspend fun toggleEnable(id: String, getState: () -> State) {
            dispatch(Result.EnableToggled(id))

            val ch = characteristicOf(ENABILITY_SERVIVE_UUID, ENABLED_CH_UUID)

            val item = getState().items.firstOrNull { it.name == id } ?: return
            val value: Byte = if (item.enabled) 1 else 0

            periph.write(ch, byteArrayOf(value.toByte()), WriteType.WithResponse)
        }
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.Loaded -> copy(items = result.items)
                is Result.EnableToggled -> this
                is Result.StateChanged -> this
                is Result.CoeffChanged -> this
            }
    }
}
