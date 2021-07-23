package me.alexander.androidApp.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.alexander.androidApp.domain.Server
import me.alexander.androidApp.domain.ServersModel

//private const val TAG = "BleConnStub"

/**
 * Заглушка доступа к BT для поиска серверов.
 *
 * Работает без наличия BT-адаптера, возвращает рандомные сервера каждые 555 мс.
 */
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
                _servers.value = ServersModel(
                    listOf(
                        Server("10:20:30:40:50:61", "Server 1", (-19..-10).random()),
                        Server("10:20:30:40:50:62", "Server 2", (-29..-20).random()),
                    )
                )
            }
        }
    }

    override fun stopScan() {
        _scanJob?.cancel()
        _scanJob = null
    }

    override fun getServerConn(id: String, scope: CoroutineScope): BleServerConn {
        val server = _servers.value.servers.first { it.address == id }
        return _serversConns[id] ?: BleServerConnStub(server.name ?: "Noname" ).also { _serversConns[id] = it }
    }
}
