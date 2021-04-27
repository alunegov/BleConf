package me.alexander.androidApp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

private const val TAG = "ServersListViewModel"

data class Server(
    val adv: Advertisement,
    val time: Long,
)

data class ServerM(
    val address: String,
    val name: String,
    val rssi: Int,
)

class ServersListViewModel : ViewModel() {
    private val _model = MutableStateFlow<List<ServerM>>(emptyList())
    val model = _model.asStateFlow()

    private val _servers = hashMapOf<String, Server>()

    init {
        Log.d(TAG, "init")
        viewModelScope.launch {
            Scanner()
                .advertisements
                //.filter { it.uuids.contains(uuidFrom("95f78395-3a98-45a3-9cc7-d71cfded4f07")) }
                .collect { adv ->
                    _servers[adv.address] = Server(adv, System.currentTimeMillis())

                    val timeThreshold = System.currentTimeMillis()
                    _servers.entries.removeIf { (it.value.time + 5000) <= timeThreshold }

                    _model.value = _servers.values.map { ServerM(it.adv.address, it.adv.name ?: "", it.adv.rssi) }.toList()
                }
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
    }
}
