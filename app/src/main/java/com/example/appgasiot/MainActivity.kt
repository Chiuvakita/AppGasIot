package com.example.appgasiot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.rememberNavController
import com.example.appgasiot.navigation.AppNav
import com.example.appgasiot.ui.theme.AppGasIotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppGasIotTheme {

                // ✅ SIEMPRE se muestra al iniciar la app
                var mostrarAvisoRed by rememberSaveable {
                    mutableStateOf(true)
                }

                if (mostrarAvisoRed) {
                    AlertDialog(
                        onDismissRequest = { mostrarAvisoRed = false },
                        title = { Text("Uso de Internet") },
                        text = {
                            Text(
                                "Esta aplicación utiliza conexión a Internet (WiFi o datos móviles) " +
                                        "para comunicarse con dispositivos IoT y sincronizar información.\n\n" +
                                        "Al continuar, aceptas el uso de red para el funcionamiento del sistema."
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                mostrarAvisoRed = false
                            }) {
                                Text("Aceptar")
                            }
                        }
                    )
                }

                val navController = rememberNavController()
                AppNav(navController)
            }
        }
    }
}
