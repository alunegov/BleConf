package me.alexander.androidApp.domain

/**
 * Описание сервера.
 *
 * @property address MAC-адрес.
 * @property name Имя.
 * @property rssi Уровень сигнала, дБ.
 */
data class Server(
    val address: String,
    val name: String?,
    val rssi: Int,
)

/**
 * Модель данных для окна Список серверов.
 *
 * @property servers Список серверов.
 * @property errorText Текст ошибки.
 */
data class ServersModel(
    val servers: List<Server> = emptyList(),
    val errorText: String = "",
)
