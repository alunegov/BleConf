package me.alexander.androidApp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.alexander.androidApp.domain.*
import java.util.concurrent.atomic.AtomicBoolean

object BleConnStub : BleConn {
    private val _servers = MutableStateFlow(ServersModel())
    override val servers: StateFlow<ServersModel> = _servers.asStateFlow()

    private var _scanJob: Job? = null

    private val _serversConns = hashMapOf<String, BleServerConn>()

    override fun startScan(scope: CoroutineScope) {
        if (_scanJob != null) {
            //logger?.d(TAG, "already scanning")
            return
        }

        _scanJob = scope.launch {
            while (true) {
                delay(555)
                _servers.value = ServersModel(listOf(
                    Server("1", "1", (-19..-10).random()),
                    Server("2", "2", (-29..-20).random()),
                ))
            }
        }
    }

    override fun stopScan() {
        _scanJob?.cancel()
        _scanJob = null
    }

    override fun getServerConn(id: String, scope: CoroutineScope): BleServerConn {
        return _serversConns[id] ?: BleServerConnStub(id).also { _serversConns[id] = it }
    }
}

class BleServerConnStub(
    override val serverName: String,
) : BleServerConn {
    private val sensors = hashMapOf<String, Sensor>(
        "1" to Sensor("1", "1", true, 2),
        "2" to Sensor("2", "2", false, 2),
        "3" to Sensor("3", "3", false, 2),
        "4" to Sensor("4", "4", false, 2),
        "5" to Sensor("5", "5", false, 2),
        "6" to Sensor("6", "6", false, 2),
        "7" to Sensor("7", "7", true, 0),
        "8" to Sensor("8", "8", true, 2, 0.3f),
    )

    private val history = mutableListOf<HistoryEvent>(
        HistoryEvent(System.currentTimeMillis() - 13000, 0b11000001),
    )

    private var conf = Conf(
        time = System.currentTimeMillis(),
    )

    override suspend fun connect() {
        // nop
    }

    override suspend fun disconnect() {
        // nop
    }

    override suspend fun getSensors(): List<Sensor> = sensors.values.toList()

    override suspend fun setSensorsEnability(sensors: List<Sensor>) {
        for (s in sensors) {
            val thisSensor = this.sensors[s.id] ?: continue
            this.sensors[s.id] = thisSensor.copy(enabled = s.enabled, state = 2)
        }

        // updating history
        if (history.size >= 5) {
            history.removeFirst()
        }

        var en = 0
        sensors.forEachIndexed { i, it -> if (it.enabled) en = en or (1 shl i) }

        history += HistoryEvent(System.currentTimeMillis(), en)
    }

    override suspend fun getHistory(): List<HistoryEvent> = history.reversed()

    override suspend fun getConf(): Conf = conf

    override suspend fun setTime(conf: Conf) {
        this.conf = this.conf.copy(time = conf.time)
    }

    override suspend fun setConfOnly(conf: Conf) {
        TODO("Not yet implemented")
    }
}
