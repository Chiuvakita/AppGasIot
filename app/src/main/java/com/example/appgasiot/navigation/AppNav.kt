package com.example.appgasiot.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appgasiot.ui.screens.LoginScreen
import com.example.appgasiot.ui.screens.HomeScreen
import com.example.appgasiot.ui.screens.RangosGasScreen
import com.example.appgasiot.ui.screens.RegisterScreen



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
    }
}
