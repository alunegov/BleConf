package com.github.alunegov.bleconf.android

import android.util.Log
import com.github.alunegov.bleconf.android.domain.Logger

/**
 * Реализация [Logger] для Android.
 */
object LoggerImpl : Logger {
    override fun d(tag: String?, msg: String) {
        Log.d(tag, msg)
    }
}
