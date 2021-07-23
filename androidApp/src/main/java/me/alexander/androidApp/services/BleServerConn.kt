package me.alexander.androidApp.services

import kotlinx.coroutines.flow.Flow
import me.alexander.androidApp.domain.Conf
import me.alexander.androidApp.domain.HistoryEvent
import me.alexander.androidApp.domain.Sensor

/**
 * Абстракция подключения к серверу (BLE-серверу)
 */
interface BleServerConn {
    /**
     * Имя сервера.
     */
    val serverName: String

    /**
     * Поток значений коэффициентов от adc-датчика (ble-notify).
     */
    val coeff: Flow<Float>

    /**
     * Подключение к серверу.
     */
    suspend fun connect()

    /**
     * Отключение от сервера.
     */
    suspend fun disconnect()

    /**
     * Возвращает список датчиков.
     *
     * @return Список датчиков или пустой список, если нет ble-сервиса STATES_SERVICE.
     */
    suspend fun getSensors(): List<Sensor>

    /**
     * Возвращает датчик по коду.
     *
     * @param id Код датчика (ble-характеристики).
     * @return Датчик или null, если нет ble-сервиса STATES_SERVICE или датчика (ble-характеристики) с указанным кодом.
     */
    suspend fun getSensor(id: String): Sensor?

    /**
     * Задаёт активные датчики (на основе sensors[].enabled).
     */
    suspend fun setSensorsEnability(sensors: List<Sensor>)

    /**
     * Возвращает историю сервера.
     */
    suspend fun getHistory(): List<HistoryEvent>

    /**
     * Возвращает настройки сервера.
     */
    suspend fun getConf(): Conf

    /**
     * Задаёт настройки сервера.
     */
    suspend fun setConf(conf: Conf)

    /**
     * Возвращает время сервера.
     *
     * @return Число секунд с 1 января 1970 (Unix Epoch).
     */
    suspend fun getTime(): Long

    /**
     * Задаёт время сервера.
     *
     * @param time Число секунд с 1 января 1970 (Unix Epoch).
     */
    suspend fun setTime(time: Long)
}
