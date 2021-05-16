package me.alexander.androidApp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

private const val TAG = "ServersListViewModel"

class ServersListViewModel(
    private val bleConn: BleConn,
) : ViewModel() {
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

    fun startScan() {
        Log.d(TAG, "startScan")
        bleConn.startScan(_bleScope)
        Log.d(TAG, "startScan post")
    }

    fun stopScan() {
        Log.d(TAG, "stopScan")
        bleConn.stopScan()
        Log.d(TAG, "stopScan post")
    }
}
