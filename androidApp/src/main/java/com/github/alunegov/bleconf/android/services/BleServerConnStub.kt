package com.github.alunegov.bleconf.android.services

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.github.alunegov.bleconf.android.domain.*

/**
 * Заглушка подключения к серверу (BLE-серверу).
 *
 * Работает без наличия BT-адаптера, возвращает рандомные датчики, историю, каждые 3 с обновляет значение коээфициента
 * adc-канала. На время жизни хранит изменения активных датчиков, историю, настройки и системное время.
 */
class BleServerConnStub(
    override val serverName: String,
) : BleServerConn {
    private val sensors = hashMapOf(
        "1" to Sensor("1", "Sensor 1", true, 2, null, false, 0),
        "2" to Sensor("2", "Sensor 2", false, 2, null, false, 1),
        "3" to Sensor("3", "Sensor 3", false, 2, null, false, 2),
        "4" to Sensor("4", "Sensor 4", false, 2, null, false, 3),
        "5" to Sensor("5", "Sensor 5", false, 2, null, false, 4),
        "6" to Sensor("6", "Sensor 6", false, 2, null, false, 5),
        "7" to Sensor("7", "Sensor 7", true, 0, null, false, 6),
        "8" to Sensor("8", "Sensor 8", true, 2, 0.3f, false, 7),
        "16" to Sensor("16", "Relay", true, 0, null, true, 15),
    )

    private val history = mutableListOf(
        HistoryEvent(System.currentTimeMillis() / 1000 - 13000, 0b1000000011000001, SensorsMaskV2Pre),
    )

    private var conf = ConfV3()

    private var time = System.currentTimeMillis() / 1000

    override val coeff: Flow<Float> = flow {
        var coeff = 0.3f
        while (true) {
            delay(3333)
            emit(coeff)
            if (coeff++ > 10.0f) {
                coeff = 0.3f
            }
        }
    }

    override suspend fun connect() {
        // nop
    }

    override suspend fun disconnect() {
        // nop
    }

    override suspend fun getSensors(): List<Sensor> = sensors.values.toList().sortedBy { it.enabledEncIndex }

    override suspend fun getSensor(id: String): Sensor? = sensors[id]

    override suspend fun setSensorsEnability(sensors: List<Sensor>) {
        for (s in sensors) {
            val thisSensor = this.sensors[s.id] ?: continue
            this.sensors[s.id] = thisSensor.copy(enabled = s.enabled/*, state = 2*/)
        }

        // updating history
        if (history.size >= 5) {
            history.removeFirst()
        }

        var en = 0
        sensors.forEach { if (it.enabled) en = en or (1 shl it.enabledEncIndex) }

        history += HistoryEvent(System.currentTimeMillis() / 1000, en, SensorsMaskV2Pre)
    }

    override suspend fun getHistory(): List<HistoryEvent> = history.reversed()

    override suspend fun getConf(): ConfBase = conf

    override suspend fun setConf(conf: ConfBase) {
        this.conf = conf as ConfV3
    }

    override suspend fun getTime(): Long = time

    override suspend fun setTime(time: Long) {
        this.time = time
    }
}
