package me.alexander.androidApp.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import me.alexander.androidApp.domain.ServersModel

interface BleConn {
    val servers: StateFlow<ServersModel>

    fun startScan(scope: CoroutineScope)

    fun stopScan()

    fun getServerConn(id: String, scope: CoroutineScope): BleServerConn
}
