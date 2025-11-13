package com.example.appgasiot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appgasiot.navigation.AppScreen
import com.example.appgasiot.data.firebase.FirebaseAuthService

@Composable
fun LoginScreen(navController: NavController) {

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                cargando = true
                FirebaseAuthService.iniciarSesion(correo, contrasena) { exito, error ->
                    cargando = false
                    if (exito) {
                        navController.navigate(AppScreen.Home.ruta) {
                            popUpTo(0)
                        }
                    } else {
                        errorMensaje = error ?: "Error iniciando sesión"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(if (cargando) "Entrando..." else "Entrar")
        }

        errorMensaje?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))

        TextButton(onClick = {
            navController.navigate(AppScreen.Register.ruta)
        }) {
            Text("¿No tienes cuenta? Registrate!")
        }
    }
}
