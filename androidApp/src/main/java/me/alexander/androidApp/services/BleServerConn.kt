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

    suspend fun setConf(conf: Conf)

    /**
     * Возвращает время сервера.
     *
     * @return Время в секундах (Unix time)
     */
    suspend fun getTime(): Long

    /**
     * Задаёт время сервера.
     *
     * @param time Время в секундах (Unix time)
     */
    suspend fun setTime(time: Long)
}
