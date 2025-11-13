package com.example.appgasiot.navigation

sealed class AppScreen(val ruta: String) {
    object Login : AppScreen("pantalla_login")
    object Register : AppScreen("pantalla_registro")
    object Home : AppScreen("pantalla_inicio")
    object RangosGas : AppScreen("pantalla_rangos_gas")
}
