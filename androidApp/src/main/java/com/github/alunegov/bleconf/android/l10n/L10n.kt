package com.github.alunegov.bleconf.android.l10n

import java.util.*

/**
 * Локализация домена, сервисов и вьюмоделей (без использования android).
 */
object L10n {
    private val bundle = ResourceBundle.getBundle(Resources::class::qualifiedName.get())

    fun tr(s: String): String {
        return bundle.getString(s)
    }
}
