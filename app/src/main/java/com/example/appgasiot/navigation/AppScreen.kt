package com.example.appgasiot.navigation

sealed class AppScreen(val ruta: String) {

    object Login : AppScreen("pantalla_login")
    object Register : AppScreen("pantalla_registro")
    object Home : AppScreen("pantalla_inicio")

    object RangosGas : AppScreen("pantalla_rangos_gas")

    // Horarios
    object HorariosCompuertaMenu : AppScreen("pantalla_horario_menu")
    object HorariosCompuerta : AppScreen("pantalla_horario_compuerta/{modo}") {
        fun crearRuta(modo: String) = "pantalla_horario_compuerta/$modo"
    }

    // historiales
    object EventosCriticos : AppScreen("pantalla_eventos_criticos")
    object HistorialLecturas : AppScreen("pantalla_historial_lecturas")
}
