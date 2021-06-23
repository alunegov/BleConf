package me.alexander.androidApp

sealed class RootScreen(val route: String) {
    object Servers : RootScreen("A")
    object Server : RootScreen("B")
}

sealed class ServerScreen(val route: String, val caption: String, val IconId: Int) {
    object Sensors : ServerScreen("B1", "Sensors", 1)
    object History : ServerScreen("B2", "History", 2)
    object Conf : ServerScreen("B3", "Conf", 3)
}
