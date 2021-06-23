package me.alexander.androidApp.services

import com.benasher44.uuid.uuidFrom
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import com.juul.kable.peripheral
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.alexander.androidApp.domain.Logger
import me.alexander.androidApp.domain.Server
import me.alexander.androidApp.domain.ServersModel

private const val TAG = "BleConnImpl"

//public expect fun mac(adv: Advertisement): String

/*public actual */fun mac(adv: Advertisement): String = adv.address

class BleConnImpl(
    val scanner: Scanner = Scanner(),
    val logger: Logger? = null,
) : BleConn {
    private val _intServers = hashMapOf<String, IntServer>()

    private val _servers = MutableStateFlow(ServersModel())
    override val servers: StateFlow<ServersModel> = _servers.asStateFlow()

    private var _scanJob: Job? = null

    override fun startScan(scope: CoroutineScope) {
        logger?.d(TAG, "startScan")

        if (_scanJob?.isActive == true) {
            logger?.d(TAG, "already scanning")
            return
        }

        _scanJob = scope.launch {
            logger?.d(TAG, "startScan launch")

            try {
                scanner
                    .advertisements
                    .filter { it.uuids.contains(uuidFrom(CONF_SERVICE_UUID)) }
                    .collect { adv ->
                        //logger?.d(TAG, "scan tick with ${adv.address}")

                        _intServers[adv.address] = IntServer(adv, System.currentTimeMillis())

                        val timeThreshold = System.currentTimeMillis()
                        _intServers.entries.removeIf { (it.value.time + 5000) <= timeThreshold }

                        _servers.value =
                            ServersModel(_intServers.values.map { Server(mac(it.adv), it.adv.name, it.adv.rssi) })
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
        val adv = _intServers[id]?.adv!!
        return BleServerConnImpl(
            adv.name ?: "noname",
            scope.peripheral(adv),
            logger,
        )
    }

    internal data class IntServer(
        val adv: Advertisement,
        val time: Long,
    )
}