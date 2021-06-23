package me.alexander.androidApp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.alexander.androidApp.domain.Conf
import me.alexander.androidApp.domain.HistoryEvent
import me.alexander.androidApp.domain.Sensor
import me.alexander.androidApp.services.BleConn

private const val TAG = "ServerViewModel"

data class SensorsModel(
    val sensors: List<Sensor> = emptyList(),
    val errorText: String = "",
)

data class HistoryModel(
    val servers: List<HistoryEvent> = emptyList(),
    val errorText: String = "",
)

data class ConfModel(
    val conf: Conf = Conf(),
    val errorText: String = "",
)

class ServerViewModel(
    private val bleConn: BleConn,
    private val address: String,
) : ViewModel() {
    private val _sensors = MutableStateFlow(SensorsModel())
    val sensors = _sensors.asStateFlow()

    private val _history = MutableStateFlow(HistoryModel())
    val history = _history.asStateFlow()

    private val _conf = MutableStateFlow(ConfModel())
    val conf = _conf.asStateFlow()

    private val _bleDispatchers = Dispatchers.IO
    private val _bleScope = CoroutineScope(viewModelScope.coroutineContext + _bleDispatchers)

    private val bleServerConn = bleConn.getServerConn(address, _bleScope)

    val serverName: String
        get() {
            return bleServerConn.serverName
        }

    init {
        Log.d(TAG, "INIT")
        /*_bleScope.launch {
            ensureConnected()
        }*/
    }

    //@OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        Log.d(TAG, "onCleared of $this")
        GlobalScope.launch(_bleDispatchers) {
            bleServerConn.disconnect()
        }
        Log.d(TAG, "onCleared post")
    }

    private suspend fun ensureConnected() {
        Log.d(TAG, "ensureConnected")
        bleServerConn.connect()
        Log.d(TAG, "ensureConnected post")
    }

    fun reloadSensors() {
        Log.d(TAG, "reloadSensors")
        _bleScope.launch {
            try {
                ensureConnected()
                _sensors.value = SensorsModel(bleServerConn.getSensors())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _sensors.value = SensorsModel(errorText = e.toString())
            }
        }
        Log.d(TAG, "reloadSensors post")
    }

    fun toggleEnabled(id: String, en: Boolean) {
        Log.d(TAG, "toggleEnabled for $id to $en")
        _bleScope.launch {
            val model = _sensors.value

            try {
                val newSensors = model.sensors.toMutableList()
                val i = newSensors.indexOfFirst { it.id == id }
                if (i == -1) return@launch
                newSensors[i] = newSensors[i].copy(enabled = en)

                _sensors.value = model.copy(sensors = newSensors)

                ensureConnected()
                bleServerConn.setSensorsEnability(newSensors)
                // sensors[id].state might change
                _sensors.value = SensorsModel(bleServerConn.getSensors())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                // TODO: reloadSensors?
                _sensors.value = model.copy(errorText = e.toString())
            }
        }
        Log.d(TAG, "toggleEnabled post")
    }

    fun reloadHistory() {
        Log.d(TAG, "reloadHistory")
        _bleScope.launch {
            try {
                ensureConnected()
                _history.value = HistoryModel(bleServerConn.getHistory())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _history.value = HistoryModel(errorText = e.toString())
            }
        }
        Log.d(TAG, "reloadHistory post")
    }

    fun reloadConf() {
        _bleScope.launch {
            try {
                ensureConnected()
                _conf.value = ConfModel(bleServerConn.getConf())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _conf.value = ConfModel(errorText = e.toString())
            }
        }
    }

    fun syncTime() {
        _bleScope.launch {
            val model = _conf.value

            _conf.value = model.copy(conf = model.conf.copy(time = System.currentTimeMillis() / 1000))

            try {
                ensureConnected()
                bleServerConn.setTime(model.conf)
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                // TODO: reloadConf?
                _conf.value = model.copy(errorText = e.toString())
            }
        }
    }

    fun setConfOnly() {
        _bleScope.launch {
            val model = _conf.value

            try {
                ensureConnected()
                bleServerConn.setConfOnly(model.conf)
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                // TODO: reloadConf?
                _conf.value = model.copy(errorText = e.toString())
            }
        }
    }
}
