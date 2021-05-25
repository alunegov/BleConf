package me.alexander.androidApp

import android.util.Log
import me.alexander.androidApp.domain.Logger

object LoggerImpl : Logger {
    override fun d(tag: String?, msg: String) {
        Log.d(tag, msg)
    }
}