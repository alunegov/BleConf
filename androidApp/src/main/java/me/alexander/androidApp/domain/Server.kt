package me.alexander.androidApp.domain

data class Server(
    val address: String,
    val name: String?,
    val rssi: Int,
)

data class ServersModel(
    val servers: List<Server> = emptyList(),
    val errorText: String = "",
)
