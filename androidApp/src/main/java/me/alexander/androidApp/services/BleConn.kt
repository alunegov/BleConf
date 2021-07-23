package me.alexander.androidApp.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import me.alexander.androidApp.domain.ServersModel

/**
 * Абстракция доступа к BT-адаптеру для поиска серверов.
 *
 * Необходимо запустить поиск серверов, найденные сервера будут возвращаться через [servers]. После "решения
 * подключиться" необходимо остановить поиск серверов и получить подключение по коду сервера.
 *
 * Корутина сканирования завершится при завершении родительского scope.
 */
interface BleConn {
    /**
     * Список серверов.
     *
     * Периодически обновляется после запуска поиска.
     */
    val servers: StateFlow<ServersModel>

    /**
     * Запускает поиск серверов.
     *
     * @param scope Scope, в котором будет запущена корутина сканирования.
     */
    fun startScan(scope: CoroutineScope)

    /**
     * Останавливает поиск серверов.
     */
    fun stopScan()

    /**
     * Возвращает подключение к серверу.
     *
     * @param id MAC-адрес сервера.
     * @param scope Scope, в котором в дальнейшем будет выполняться работа с сервером.
     * @return Подключение к серверу.
     */
    fun getServerConn(id: String, scope: CoroutineScope): BleServerConn
}
