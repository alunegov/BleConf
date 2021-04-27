package me.alexander.androidApp

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Peripheral
import com.juul.kable.WriteType
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "ServerViewModel"

// код ble-сервиса настройки
private const val ENABILITY_SERVIVE_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f07";
// код ble-характеристики набора активных датчиков (g...Sensors[].enabled кодируется в uint8_t)
private const val ENABLED_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f17";
// код ble-характеристики истории изменений (кодируется в строку, только чтение)
private const val UPDATES_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f27";

// код ble-сервиса оповещения
private const val STATES_SERVIVE_UUID = "4834cad6-5043-4ed9-8d85-a277e72c8178";  // ref sensor

data class SensorM(
    val enabled: Boolean,
    val name: String,
    val state: Int,
)

class ServerViewModel(
    private val address: String,
) : ViewModel() {
    private val _sensors = MutableStateFlow<List<SensorM>>(emptyList())
    val sensors = _sensors.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history = _history.asStateFlow()

    private val _conf = MutableStateFlow<List<String>>(emptyList())
    val conf = _conf.asStateFlow()

    lateinit var periph: Peripheral

    init {
        Log.d(TAG, "init")
        viewModelScope.launch {
            periph = peripheral(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address))

            periph.connect()
            Log.d(TAG, "connected")

            reloadSensors()
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        viewModelScope.launch {
            withTimeoutOrNull(5000) {
                Log.d(TAG, "disconnecting...")
                periph.disconnect()
                Log.d(TAG, "disconnected")
            }
        }
    }

    fun reloadSensors() {
        Log.d(TAG, "reloadSensors")
        viewModelScope.launch {
            val statesService = periph.services?.firstOrNull { it.serviceUuid == uuidFrom(STATES_SERVIVE_UUID) }
            if (statesService == null) {
                _sensors.value = listOf()
                return@launch
            }

            val newSensors = mutableListOf<SensorM>()

            val enabledEnc = periph.read(characteristicOf(ENABILITY_SERVIVE_UUID, ENABLED_CH_UUID))
            Log.d(TAG, "enabledEnc = " + enabledEnc[0].toUByte().toString())

            statesService.characteristics.forEachIndexed { i, it ->
                // TODO: unstable index i
                val enabled = (enabledEnc[0].toInt() and (1 shl i)) != 0
                val state = periph.read(it)
                newSensors += SensorM(enabled, it.characteristicUuid.toString(), state[0].toInt())
            }

            _sensors.value = newSensors
        }
    }

    fun toggleEnabled(name: String, en: Boolean) {
        Log.d(TAG, "toggleEnabled for $name to $en")
        viewModelScope.launch {
            val newSensors = _sensors.value.toMutableList()
            val i = newSensors.indexOfFirst { it.name == name }
            if (i == -1) return@launch
            newSensors[i] = newSensors[i].copy(enabled = en)

            _sensors.value = newSensors

            var enabledEnc = 0
            newSensors.forEachIndexed { i, it ->
                // TODO: unstable index i
                if (it.enabled) enabledEnc = enabledEnc or (1 shl i)
            }
            periph.write(characteristicOf(ENABILITY_SERVIVE_UUID, ENABLED_CH_UUID), byteArrayOf(enabledEnc.toByte()), WriteType.WithResponse)
            // TODO: reread STATES_SERVIVE_UUID, [name].state
        }
    }
}
