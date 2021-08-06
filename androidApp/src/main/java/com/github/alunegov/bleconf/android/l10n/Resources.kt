package com.github.alunegov.bleconf.android.l10n

import java.util.*

class Resources: ListResourceBundle() {
    override fun getContents(): Array<Array<Any>> {
        return arrayOf(
            arrayOf("noname", "Noname"),
            arrayOf("wrong_pwd", "Wrong password"),
        )
    }
}

class Resources_ru: ListResourceBundle() {
    override fun getContents(): Array<Array<Any>> {
        return arrayOf(
            arrayOf("noname", "Noname"),
            arrayOf("wrong_pwd", "Пароль указан неверно"),
        )
    }
}
