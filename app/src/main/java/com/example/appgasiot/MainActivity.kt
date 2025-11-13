package com.example.appgasiot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.appgasiot.navigation.AppNav
import com.example.appgasiot.ui.theme.AppGasIotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppGasIotTheme {
                val navController = rememberNavController()
                AppNav(navController)
            }
        }
    }
}
