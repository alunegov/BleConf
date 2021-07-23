package me.alexander.androidApp.domain

/**
 * Абстракция логгера для использования на уровне сервисов.
 */
interface Logger {
    /**
     * Send a DEBUG log message.
     */
    fun d(tag: String?, msg: String)
}
