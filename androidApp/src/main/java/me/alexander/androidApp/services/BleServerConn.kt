package me.alexander.androidApp.services

import me.alexander.androidApp.domain.Conf
import me.alexander.androidApp.domain.HistoryEvent
import me.alexander.androidApp.domain.Sensor

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
