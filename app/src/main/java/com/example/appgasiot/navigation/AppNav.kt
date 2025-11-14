package com.example.appgasiot.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType

import com.example.appgasiot.ui.screens.*

@Composable
fun AppNav(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = AppScreen.Login.ruta
    ) {

        composable(AppScreen.Login.ruta) { LoginScreen(navController) }
        composable(AppScreen.Register.ruta) { RegisterScreen(navController) }
        composable(AppScreen.Home.ruta) { HomeScreen(navController) }

        composable(AppScreen.RangosGas.ruta) { RangosGasScreen(navController) }

        // Horarios menú
        composable(AppScreen.HorariosCompuertaMenu.ruta) {
            HorariosCompuertaMenuScreen(navController)
        }

        // Horarios con parámetro modo
        composable(
            route = "pantalla_horario_compuerta/{modo}",
            arguments = listOf(
                navArgument("modo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val modo = backStackEntry.arguments?.getString("modo")
            HorariosCompuertaScreen(navController, modo)
        }

        // NUEVAS PANTALLAS
        composable(AppScreen.EventosCriticos.ruta) { EventosCriticosScreen(navController) }
        composable(AppScreen.HistorialLecturas.ruta) { HistorialLecturasScreen(navController) }
    }
}
