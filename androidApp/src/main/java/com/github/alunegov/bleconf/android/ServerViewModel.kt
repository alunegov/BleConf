package com.github.alunegov.bleconf.android

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.github.alunegov.bleconf.android.domain.Conf
import com.github.alunegov.bleconf.android.domain.HistoryEvent
import com.github.alunegov.bleconf.android.domain.Sensor
import com.github.alunegov.bleconf.android.l10n.L10n
import com.github.alunegov.bleconf.android.services.BleConn
import kotlin.math.abs

private const val TAG = "ServerViewModel"

/**
 * Пароль для просмотра/редактирования настроек сервера, хэшированный с помощью bcrypt.
 *
 * Текущий пароль - 7777.
 * Онлайн-генератор - https://bcrypt-generator.com/.
 */
private const val CONF_PWD_HASH = "\$2y\$12\$XT2u67B54nlKsow0QOtNsOVAHVJETV/k6GFp0weZ0/DElOqF9r5yS"

/**
 * Модель данных для окна Датчики.
 *
 * @property sensors Список датчиков.
 * @property errorText Текст ошибки.
 * @property loading Флаг: Первоначальная загрузка данных.
 */
data class SensorsModel(
    val sensors: List<Sensor> = emptyList(),
    val errorText: String = "",
    val loading: Boolean = false,
)

/**
 * Модель данных для окна История.
 *
 * @property events Список событий.
 * @property errorText Текст ошибки.
 * @property loading Флаг: Первоначальная загрузка данных.
 */
data class HistoryModel(
    val events: List<HistoryEvent> = emptyList(),
    val errorText: String = "",
    val loading: Boolean = false,
)

/**
 * Модель данных для окна Настройки.
 *
 * @property isAuthed Флаг: Авторизация выполнена.
 * @property conf Настройки.
 * @property errorText Текст ошибки.
 * @property loading Флаг: Первоначальная загрузка данных.
 */
data class ConfModel(
    val isAuthed: Boolean = false,
    val conf: Conf = Conf(),
    val errorText: String = "",
    val loading: Boolean = false,
)

/**
 * Модель данных со временем для окон Датчики и Настройки.
 *
 * @property time Системное время.
 * @property errorText Текст ошибки.
 * @property loading Флаг: Первоначальная загрузка данных.
 */
data class TimeModel(
    val time: Long = 0,
    val errorText: String = "",
    val loading: Boolean = false,
)

/**
 * VM окон сервера: Датчики, История и Настройки.
 *
 * Пользователи должны подписаться на:
 * - [sensors] и вызвать [reloadSensors], изменение активности через [setEnabled]
 * - [history] и вызвать [reloadHistory]
 * - [conf] и вызвать [reloadConf] (перед этим нужно авторизироваться с помощью [authConf]), изменение настроек через [setConf]
 * - [time] и вызвать [reloadTime], синхронизация времени через [syncTime].
 *
 * @property bleConn Реализация [BleConn].
 * @property address MAC-адрес сервера.
 */
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

    /**
     * Имя сервера.
     */
    val serverName: String
        get() = bleServerConn.serverName

    init {
        Log.d(TAG, "INIT")
        /*_bleScope.launch {
            ensureConnected()
        }*/
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        Log.d(TAG, "onCleared of $this")
        GlobalScope.launch(_bleDispatchers) {
            bleServerConn.disconnect()
        }
    }

    /**
     * Подключение к серверу.
     */
    private suspend fun ensureConnected() {
        Log.d(TAG, "ensureConnected")
        bleServerConn.connect()
        Log.d(TAG, "ensureConnected post")
    }

    /**
     * Загружает датчики [sensors]. Также запускает корутину отслеживания коэффициента adc-канала.
     */
    fun reloadSensors() {
        Log.d(TAG, "reloadSensors")
        _bleScope.launch {
            try {
                _sensors.value = _sensors.value.copy(loading = true)

                ensureConnected()
                _sensors.value = SensorsModel(sensors = bleServerConn.getSensors())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _sensors.value = SensorsModel(errorText = e.message ?: e.toString())
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
        }
    }

    /**
     * Прекращает "наблюдение" за датчиками - останавливает корутину отслеживания коэффициента adc-канала.
     */
    fun stopObserveSensors() {
        Log.d(TAG, "stopObserveSensors")
        _coeffCollectJob?.cancel()
        _coeffCollectJob = null
        Log.d(TAG, "stopObserveSensors post")
    }

    /**
     * Задаёт флаг активности для датчика.
     */
    fun setEnabled(id: String, en: Boolean) {
        Log.d(TAG, "toggleEnabled for $id to $en")
        assert(!_sensors.value.loading)
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
                _sensors.value = model.copy(errorText = e.message ?: e.toString())
            }
        }
    }

    /**
     * Загружает историю [history].
     */
    fun reloadHistory() {
        Log.d(TAG, "reloadHistory")
        _bleScope.launch {
            try {
                _history.value = _history.value.copy(loading = true)

                ensureConnected()
                _history.value = HistoryModel(events = bleServerConn.getHistory())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _history.value = HistoryModel(errorText = e.message ?: e.toString())
            }
        }
    }

    /**
     * Авторизация для просмотра/редактирования системных настроек сервера.
     *
     * @param pwd Введённый пароль.
     */
    fun authConf(pwd: String) {
        Log.d(TAG, "authConf")
        val res = BCrypt.verifyer().verify(pwd.toCharArray(), CONF_PWD_HASH)
        if (res.verified) {
            _conf.value = ConfModel(isAuthed = true)
        } else {
            _conf.value = ConfModel(isAuthed = false, errorText = L10n.tr("wrong_pwd"))
        }
    }

    /**
     * Загружает настройки [conf].
     */
    fun reloadConf() {
        Log.d(TAG, "reloadConf")
        assert(_conf.value.isAuthed)
        _bleScope.launch {
            try {
                _conf.value = _conf.value.copy(loading = true)

                ensureConnected()
                _conf.value = ConfModel(isAuthed = true, conf = bleServerConn.getConf())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _conf.value = ConfModel(isAuthed = true, errorText = e.message ?: e.toString())
            }
        }
    }

    /**
     * Задаёт настройки.
     */
    fun setConf(conf: Conf) {
        Log.d(TAG, "setConf")
        assert(_conf.value.isAuthed)
        assert(!_conf.value.loading)
        _bleScope.launch {
            val model = _conf.value

            try {
                _conf.value = model.copy(conf = conf)

                ensureConnected()
                bleServerConn.setConf(conf)
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                // TODO: reloadConf?
                _conf.value = model.copy(errorText = e.message ?: e.toString())
            }
        }
    }

    /**
     * Загружает время на сервере [time].
     */
    fun reloadTime() {
        Log.d(TAG, "reloadTime")
        _bleScope.launch {
            try {
                _time.value = _time.value.copy(loading = true)

                ensureConnected()
                _time.value = TimeModel(time = bleServerConn.getTime())
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                _time.value = TimeModel(errorText = e.message ?: e.toString())
            }
        }
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
                _time.value = model.copy(errorText = e.message ?: e.toString(), loading = false)
            }
        }
    }
}

fun serverViewModelFactory(bleConn: BleConn, address: String): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ServerViewModel(bleConn, address) as T
        }
    }
}
