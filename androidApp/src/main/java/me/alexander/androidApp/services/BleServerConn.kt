package me.alexander.androidApp.services

import kotlinx.coroutines.flow.Flow
import me.alexander.androidApp.domain.Conf
import me.alexander.androidApp.domain.HistoryEvent
import me.alexander.androidApp.domain.Sensor

interface BleServerConn {
    val serverName: String

    val coeff: Flow<Float>

    suspend fun connect()

    suspend fun disconnect()

    /**
     * Возвращает список датчиков
     *
     * @return Список датчиков или пустой список, если нет ble-сервиса STATES_SERVICE
     */
    suspend fun getSensors(): List<Sensor>

    /**
     * Возвращает датчик по коду
     *
     * @param id Код датчика (ble-характеристики)
     *
     * @return Датчик или null, если нет ble-сервиса STATES_SERVICE или датчика (ble-характеристики) с указанным кодом
     */
    suspend fun getSensor(id: String): Sensor?

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
