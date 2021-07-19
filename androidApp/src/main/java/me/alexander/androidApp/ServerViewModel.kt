package me.alexander.androidApp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.alexander.androidApp.domain.Conf
import me.alexander.androidApp.domain.HistoryEvent
import me.alexander.androidApp.domain.Sensor
import me.alexander.androidApp.services.BleConn
import kotlin.math.abs

private const val TAG = "ServerViewModel"

private const val CONF_PWD = "7777"

data class SensorsModel(
    val sensors: List<Sensor> = emptyList(),
    val errorText: String = "",
    val loading: Boolean = false,
)

data class HistoryModel(
    val events: List<HistoryEvent> = emptyList(),
    val errorText: String = "",
    val loading: Boolean = false,
)

data class ConfModel(
    val isAuthed: Boolean = false,
    val conf: Conf = Conf(),
    val errorText: String = "",
    val loading: Boolean = false,
)

data class TimeModel(
    val time: Long = 0,
    val errorText: String = "",
    val loading: Boolean = false,
)

class ServerViewModel(
    bleConn: BleConn,
    address: String,
) : ViewModel() {
    private val _sensors = MutableStateFlow(SensorsModel(loading = true))
    val sensors = _sensors.asStateFlow()

    private val _history = MutableStateFlow(HistoryModel(loading = true))
    val history = _history.asStateFlow()

    private val _conf = MutableStateFlow(ConfModel(loading = true))
    val conf = _conf.asStateFlow()

    private val _time = MutableStateFlow(TimeModel(loading = true))
    val time = _time.asStateFlow()

    private val _bleDispatchers = Dispatchers.IO
    private val _bleScope = CoroutineScope(viewModelScope.coroutineContext + _bleDispatchers)

    private val bleServerConn = bleConn.getServerConn(address, _bleScope)

    private var _coeffCollectJob: Job? = null

    val serverName: String
        get() = bleServerConn.serverName

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
            val model = _sensors.value
            if (model.sensors.isEmpty()) {
                _sensors.value = model.copy(loading = true)
            }

            try {
                //_sensors.value = SensorsModel(loading = true)

                ensureConnected()
                _sensors.value = SensorsModel(sensors = bleServerConn.getSensors())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _sensors.value = SensorsModel(errorText = e.toString())
            }
        }
        if (_coeffCollectJob?.isActive != true) {
            Log.d(TAG, "coeffCollect")
            _coeffCollectJob = _bleScope.launch {
                Log.d(TAG, "coeffCollect launch")
                try {
                    bleServerConn.coeff.collect { coeff ->
                        val model = _sensors.value

                        val newSensors = model.sensors.toMutableList()
                        val i = newSensors.indexOfFirst { it.coeff != null }
                        if (i == -1) return@collect
                        newSensors[i] = newSensors[i].copy(coeff = coeff)

                        _sensors.value = model.copy(sensors = newSensors)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                    // TODO: pass error to _sensors?
                }
                Log.d(TAG, "coeffCollect launch post")
            }
            Log.d(TAG, "coeffCollect post")
        }
        Log.d(TAG, "reloadSensors post")
    }

    fun stopObserveSensors() {
        Log.d(TAG, "stopObserveSensors")
        _coeffCollectJob?.cancel()
        _coeffCollectJob = null
        Log.d(TAG, "stopObserveSensors post")
    }

    fun setEnabled(id: String, en: Boolean) {
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

                // sensors[id].state might change. using new object (other than newSensors) to trigger _sensors update
                val sensors = model.sensors.toMutableList()
                sensors[i] = bleServerConn.getSensor(id) ?: return@launch
                _sensors.value = SensorsModel(sensors = sensors)
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
            val model = _history.value
            if (model.events.isEmpty()) {
                _history.value = model.copy(loading = true)
            }

            try {
                //_history.value = HistoryModel(loading = true)

                ensureConnected()
                _history.value = HistoryModel(events = bleServerConn.getHistory())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _history.value = HistoryModel(errorText = e.toString())
            }
        }
        Log.d(TAG, "reloadHistory post")
    }

    // Авторизация для просмотра/редактирования системных настроек сервера
    fun authConf(pwd: String) {
        Log.d(TAG, "authConf")
        // TODO: encrypt pwd
        if (pwd == CONF_PWD) {
            _conf.value = ConfModel(isAuthed = true)
        } else {
            _conf.value = ConfModel(isAuthed = false, errorText = "Wrong password")  // TODO: l10n
        }
    }

    fun reloadConf() {
        Log.d(TAG, "reloadConf")
        assert(_conf.value.isAuthed)
        _bleScope.launch {
            try {
                //_conf.value = ConfModel(isAuthed = true, loading = true)

                ensureConnected()
                _conf.value = ConfModel(isAuthed = true, conf = bleServerConn.getConf())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _conf.value = ConfModel(isAuthed = true, errorText = e.toString())
            }
        }
        Log.d(TAG, "reloadConf post")
    }

    fun setConf(conf: Conf) {
        Log.d(TAG, "setConf")
        assert(_conf.value.isAuthed)
        _bleScope.launch {
            val model = _conf.value

            try {
                _conf.value = model.copy(conf = conf)

                ensureConnected()
                bleServerConn.setConf(conf)
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                // TODO: reloadConf?
                _conf.value = model.copy(errorText = e.toString())
            }
        }
        Log.d(TAG, "setConf post")
    }

    fun reloadTime() {
        Log.d(TAG, "reloadTime")
        _bleScope.launch {
            try {
                //_time.value = TimeModel(loading = true)

                ensureConnected()
                _time.value = TimeModel(time = bleServerConn.getTime())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _time.value = TimeModel(errorText = e.toString())
            }
        }
        Log.d(TAG, "reloadTime post")
    }

    /**
     * Синхронизирует время на сервере, используя текущее время (телефона). Синхронизация выполняется только если
     * время отличается более чем на секунду.
     */
    fun syncTime() {
        Log.d(TAG, "syncTime")
        _bleScope.launch {
            val model = _time.value

            try {
                val ourTime = System.currentTimeMillis() / 1000

                _time.value = TimeModel(time = ourTime)

                ensureConnected()
                if (abs(bleServerConn.getTime() - ourTime) > 1) {
                    bleServerConn.setTime(ourTime)
                }
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _time.value = model.copy(errorText = e.toString())
            }
        }
        Log.d(TAG, "syncTime post")
    }
}
