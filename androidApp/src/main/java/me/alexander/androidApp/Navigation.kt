package me.alexander.androidApp

/**
 * Роуты главного окна.
 *
 * @property route Имя роута.
 */
sealed class RootScreen(val route: String) {
    /**
     * Роут окна Список серверов.
     */
    object Servers : RootScreen("A")

    /**
     * Роут окна сервера.
     */
    object Server : RootScreen("B")
}

/**
 * Роуты окна сервера.
 *
 * @property route Имя роута.
 * @property captionResId Код ресурса с названием пути.
 * @property IconId Код иконки, см. [me.alexander.androidApp.ui_compose.ServerBottomBar].
 */
sealed class ServerScreen(val route: String, val captionResId: Int, val IconId: Int) {
    /**
     * Роут окна Датчики.
     */
    object Sensors : ServerScreen("B1", R.string.server_sensors, 1)

    /**
     * Роут окна История.
     */
    object History : ServerScreen("B2", R.string.server_history, 2)

    /**
     * Роут окна Настройки.
     */
    object Conf : ServerScreen("B3", R.string.server_conf, 3)
}
