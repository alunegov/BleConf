package me.alexander.androidApp

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.bytes
import com.benasher44.uuid.uuidFrom
import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.alexander.androidApp.domain.*
import java.nio.ByteBuffer

private const val TAG = "BleConnImpl"

interface BleConn {
    val servers: StateFlow<ServersModel>

    fun startScan(scope: CoroutineScope)

    fun stopScan()

    fun getServerConn(id: String, scope: CoroutineScope): BleServerConn
}

interface BleServerConn {
    val serverName: String

    suspend fun connect()

    suspend fun disconnect()

    suspend fun getSensors(): List<Sensor>

    suspend fun setSensorsEnability(sensors: List<Sensor>)

    suspend fun getHistory(): List<HistoryEvent>

    suspend fun getConf(): Conf

    suspend fun setTime(conf: Conf)

    suspend fun setConfOnly(conf: Conf)
}

//public expect fun mac(adv: Advertisement): String

/*public actual */fun mac(adv: Advertisement): String = adv.address

// код ble-сервиса настройки
internal const val CONF_SERVICE_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f07"
// код ble-характеристики набора активных датчиков (g...Sensors[].enabled кодируется в uint8_t)
internal const val CONF_ENABLED_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f17"
// код ble-характеристики истории изменений (кодируется в строку, только чтение)
internal const val CONF_UPDATES_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f27"
internal const val CONF_TIME_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f37"
//internal const val CONF_CONF_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4f47"
internal const val CONF_COEFF_CH_UUID = "95f78395-3a98-45a3-9cc7-d71cfded4ff7"

// код ble-сервиса оповещения
internal const val STATES_SERVICE_UUID = "4834cad6-5043-4ed9-8d85-a277e72c8178"  // ref sensor
//internal const val STATES_SENSORx_CH_UUID

object BleConnImpl : BleConn {
    var logger: Logger? = null

    private val _intServers = hashMapOf<String, IntServer>()

    private val _servers = MutableStateFlow(ServersModel())
    override val servers: StateFlow<ServersModel> = _servers.asStateFlow()

    private val _scanner = Scanner()
    private var _scanJob: Job? = null

    override fun startScan(scope: CoroutineScope) {
        logger?.d(TAG, "startScan")

        if (_scanJob != null) {
            logger?.d(TAG, "already scanning")
            return
        }

        _scanJob = scope.launch {
            logger?.d(TAG, "startScan launch")

            try {
                _scanner
                    .advertisements
                    .filter { it.uuids.contains(uuidFrom(CONF_SERVICE_UUID)) }
                    .collect { adv ->
                        //logger?.d(TAG, "scan tick with ${adv.address}")

                        _intServers[adv.address] = IntServer(adv, System.currentTimeMillis())

                        val timeThreshold = System.currentTimeMillis()
                        _intServers.entries.removeIf { (it.value.time + 5000) <= timeThreshold }

                        _servers.value = ServersModel(_intServers.values.map { Server(mac(it.adv), it.adv.name, it.adv.rssi) })
                    }
            } catch (ce: CancellationException) {
                logger?.d(TAG, ce.toString())
            } catch (e: Exception) {
                logger?.d(TAG, e.toString())
                _servers.value = ServersModel(errorText = e.toString())
            }

            logger?.d(TAG, "startScan launch post")
        }

        logger?.d(TAG, "startScan post")
    }

    override fun stopScan() {
        logger?.d(TAG, "stopScan")
        _scanJob?.cancel()
        _scanJob = null
        logger?.d(TAG, "stopScan post")
    }

    override fun getServerConn(id: String, scope: CoroutineScope): BleServerConn {
        logger?.d(TAG, "getServerConn")
        // TODO: check id presence
        return BleServerConnImpl(
            _intServers[id]?.adv!!.name ?: "noname",
            scope.peripheral(_intServers[id]?.adv!!),
            logger,
        )
    }

    internal data class IntServer(
        val adv: Advertisement,
        val time: Long,
    )
}

class BleServerConnImpl(
    override val serverName: String,
    private val periph: Peripheral,
    private val logger: Logger? = null,
) : BleServerConn {
    override suspend fun connect() {
        logger?.d(TAG, "connect")
        // TODO: withTimeoutOrNull
        periph.connect()
    }

    override suspend fun disconnect() {
        logger?.d(TAG, "disconnect")
        withTimeoutOrNull(5000) {
            logger?.d(TAG, "disconnecting...")
            periph.disconnect()
            logger?.d(TAG, "disconnected")
        }
    }

    override suspend fun getSensors(): List<Sensor> {
        logger?.d(TAG, "getSensors")

        val statesService = periph.services?.firstOrNull { it.serviceUuid == uuidFrom(STATES_SERVICE_UUID) } ?: return emptyList()

        val newSensors = mutableListOf<Sensor>()

        val enabledEncRaw = periph.read(characteristicOf(CONF_SERVICE_UUID, CONF_ENABLED_CH_UUID))
        logger?.d(TAG, "enabledEnc = " + enabledEncRaw[0].toString())

        statesService.characteristics.forEachIndexed { i, it ->
            // TODO: unstable index i
            val enabled = (enabledEncRaw[0].toInt() and (1 shl i)) != 0
            val stateRaw = periph.read(it)
            var coeff: Float? = null
            // TODO: adc chann descriptor?
            if (i == 7) {
                val coeffRaw = periph.read(characteristicOf(CONF_SERVICE_UUID, CONF_COEFF_CH_UUID))
                coeff = ByteBuffer.wrap(coeffRaw).getFloat()
            }

            newSensors += Sensor(
                it.characteristicUuid.toString(),
                extractName(it.characteristicUuid),
                enabled,
                stateRaw[0].toInt(),
                coeff,
            )
        }

        return newSensors
    }

    private fun extractName(id: Uuid): String {
        return ByteBuffer.wrap(id.bytes).getLong(8).toString()
    }

    override suspend fun setSensorsEnability(sensors: List<Sensor>) {
        logger?.d(TAG, "setSensorsEnability")

        var enabledEnc = 0
        sensors.forEachIndexed { i, it ->
            // TODO: unstable index i
            if (it.enabled) enabledEnc = enabledEnc or (1 shl i)
        }

        periph.write(characteristicOf(CONF_SERVICE_UUID, CONF_ENABLED_CH_UUID), byteArrayOf(enabledEnc.toByte()), WriteType.WithResponse)
    }

    override suspend fun getHistory(): List<HistoryEvent> {
        logger?.d(TAG, "getHistory")

        val historyEncRaw = periph.read(characteristicOf(CONF_SERVICE_UUID, CONF_UPDATES_CH_UUID))
        logger?.d(TAG, "historyEnc = " + historyEncRaw.decodeToString())

        return decodeHistory(historyEncRaw)
    }

    // example: "time: 587093, en: 193; time: 587088, en: 225; time: 518671, en: 193; time: 518670, en: 195; time: 511483, en: 193;"
    private fun decodeHistory(historyEncRaw: ByteArray): List<HistoryEvent> {
        val asStr = historyEncRaw.decodeToString()
        // TODO: use json deserialization
        val events = mutableListOf<HistoryEvent>()
        val eventsAsStr = asStr.split(';')
        for (eventAsStr in eventsAsStr) {
            val propsAsStr = eventAsStr.split(',')
            if (propsAsStr.size != 2) break  // last one

            // TODO: check bounds
            val time = propsAsStr[0].split(':')[1].trim().toLong()
            val en = propsAsStr[1].split(':')[1].trim().toInt()

            events += HistoryEvent(time, en)
        }
        return events
    }

    override suspend fun getConf(): Conf {
        logger?.d(TAG, "getConf")

        val timeRaw = periph.read(characteristicOf(CONF_SERVICE_UUID, CONF_TIME_CH_UUID)).also { it.reverse() }
        val time = ByteBuffer.wrap(timeRaw).getLong()

        //val confRaw = periph.read(characteristicOf(CONF_SERVICE_UUID, CONF_CONF_CH_UUID))
        //val dummy = ByteBuffer.wrap(confRaw).getInt()
        val dummy = 0

        return Conf(time, dummy)
    }

    override suspend fun setTime(conf: Conf) {
        logger?.d(TAG, "setTime to ${conf.time}")
        val timeRaw = ByteBuffer.allocate(java.lang.Long.BYTES).putLong(conf.time).array().also { it.reverse() }
        periph.write(characteristicOf(CONF_SERVICE_UUID, CONF_TIME_CH_UUID), timeRaw, WriteType.WithResponse)
    }

    override suspend fun setConfOnly(conf: Conf) {
        logger?.d(TAG, "setConf")
        // TODO: encode conf
        //val confRaw = ByteBuffer.allocate(1).array()
        //periph.write(characteristicOf(CONF_SERVICE_UUID, CONF_CONF_CH_UUID), confRaw, WriteType.WithResponse)
    }
}
