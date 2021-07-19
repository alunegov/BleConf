package me.alexander.androidApp

sealed class RootScreen(val route: String) {
    object Servers : RootScreen("A")
    object Server : RootScreen("B")
}

sealed class ServerScreen(val route: String, val captionResId: Int, val IconId: Int) {
    object Sensors : ServerScreen("B1", R.string.server_sensors, 1)
    object History : ServerScreen("B2", R.string.server_history, 2)
    object Conf : ServerScreen("B3", R.string.server_conf, 3)
}
