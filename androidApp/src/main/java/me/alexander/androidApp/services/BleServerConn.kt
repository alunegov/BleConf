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
     * @return Число секунд с 1 января 1970 (Unix Epoch)
     */
    suspend fun getTime(): Long

    /**
     * Задаёт время сервера.
     *
     * @param time Число секунд с 1 января 1970 (Unix Epoch)
     */
    suspend fun setTime(time: Long)
}
