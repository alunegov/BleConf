package me.alexander.androidApp.domain

/**
 * Событие истории сервера (изменение активных датчиков).
 *
 * Аналог структуры BleApp::Server::Services::UpdateData в проекте сервера.
 *
 * @property time Время изменения, UNIX-time.
 * @property en Активные датчики, в виде битовой маски (0b00100001 -> включены 1-ый и 6-ой датчики).
 */
data class HistoryEvent(
    val time: Long,
    val en: Int,
)
