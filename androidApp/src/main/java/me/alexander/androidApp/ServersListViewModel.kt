package me.alexander.androidApp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.alexander.androidApp.services.BleConn

private const val TAG = "ServersListViewModel"

/**
 * VM окна Список серверов.
 *
 * Пользователи должны подписаться на [servers] и вызвать [startScan].
 *
 * @property bleConn Реализация [BleConn].
 */
class ServersListViewModel(
    private val bleConn: BleConn,
) : ViewModel() {
    /**
     * Список серверов.
     *
     * Периодически обновляется после запуска поиска.
     */
    val servers = bleConn.servers

    private val _bleDispatchers = Dispatchers.IO
    private val _bleScope = CoroutineScope(viewModelScope.coroutineContext + _bleDispatchers)

    init {
        Log.d(TAG, "INIT")
        //startScan()
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        //stopScan()
        Log.d(TAG, "onCleared post")
    }

    /**
     * Запускает поиск серверов.
     */
    fun startScan() {
        Log.d(TAG, "startScan")
        bleConn.startScan(_bleScope)
        Log.d(TAG, "startScan post")
    }

    /**
     * Останавливает поиск серверов.
     */
    fun stopScan() {
        Log.d(TAG, "stopScan")
        bleConn.stopScan()
        Log.d(TAG, "stopScan post")
    }
}
