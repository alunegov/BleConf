package com.github.alunegov.bleconf.android.services

import com.benasher44.uuid.uuidFrom
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import com.juul.kable.peripheral
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.github.alunegov.bleconf.android.domain.Logger
import com.github.alunegov.bleconf.android.domain.Server
import com.github.alunegov.bleconf.android.domain.ServersModel

private const val TAG = "BleConnImpl"

//public expect fun mac(adv: Advertisement): String

/**
 * Извлекает MAC-адрес сервера из его "рекламы".
 */
/*public actual */fun mac(adv: Advertisement): String = adv.address

private const val ServerTimeoutThreshold = 5000

/**
 * Реализация доступа к BT-адаптеру для поиска серверов через Kable.
 *
 * "Рекламирующиеся" сервера из потока от сканнера помещаются в список [_intServers]. При каждом обновлении он
 * "выдаётся" наружу через [servers], и происходит отбрасывание серверов, от которых не было "реклам" в течении 5 с.
 *
 * @property scanner Реализация [Scanner].
 * @property logger Реализация [Logger].
 */
class BleConnImpl(
    private val scanner: Scanner = Scanner(),
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

        // resetting time to not exceed threshold on consecutive scans
        _intServers.values.forEach { it.time = System.currentTimeMillis() }

        _scanJob = scope.launch {
            logger?.d(TAG, "startScan launch")

            try {
                scanner
                    .advertisements
                    //.filter { it.uuids.contains(uuidFrom(CONF_SERVICE_UUID)) }
                    .collect { adv ->
                        //logger?.d(TAG, "scan tick with ${adv.address}")

                        // делаем фильтрацию "наших" устройств здесь, а не перед collect, п.ч. при "исчезновении"
                        // последнего устройства оно навсегда останется в списке (список не обнулится)
                        if (adv.uuids.contains(uuidFrom(CONF_SERVICE_UUID)) or adv.uuids.contains(uuidFrom(STATES_SERVICE_UUID))) {
                            _intServers[adv.address] = IntServer(adv, System.currentTimeMillis())
                        }

                        val timeThreshold = System.currentTimeMillis()
                        _intServers.entries.removeIf { (it.value.time + ServerTimeoutThreshold) <= timeThreshold }

                        _servers.value = ServersModel(servers = _intServers.values.map { Server(mac(it.adv), it.adv.name, it.adv.rssi) })
                    }
            } catch (ce: CancellationException) {
                logger?.d(TAG, ce.toString())
            } catch (e: Exception) {
                logger?.d(TAG, e.toString())
                _servers.value = ServersModel(errorText = e.message ?: e.toString())
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
        logger?.d(TAG, "getServerConn for $id")
        //logger?.d(TAG, "_intServers $_intServers")
        // TODO: check id presence
        val adv = _intServers[id]?.adv!!
        return BleServerConnImpl(
            adv.name ?: "Noname",
            scope.peripheral(adv),
            logger,
        )
    }

    /**
     * Внутренне описание сервера.
     *
     * @param adv "Реклама" сервера.
     * @param time Время получения последней "рекламы".
     */
    internal data class IntServer(
        val adv: Advertisement,
        var time: Long,
    )
}
