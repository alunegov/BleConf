package me.alexander.shared.sensors.store

import com.arkivanov.mvikotlin.core.store.*
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.juul.kable.Scanner
import kotlinx.coroutines.flow.onEach
import me.alexander.shared.sensors.Sensor
import me.alexander.shared.sensors.store.SensorsStore.Intent
import me.alexander.shared.sensors.store.SensorsStore.State

internal class SensorsStoreProvider(
    private val storeFactory: StoreFactory
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
        data class First(val items: List<Sensor>) : Result()
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Unit, State, Result, Nothing>() {
        override suspend fun executeAction(action: Unit, getState: () -> State) {

        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            Scanner()
                .advertisements
                .onEach { it.address }
        }
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when(result) {
                is Result.First -> copy(items = result.items)
            }
    }
}
