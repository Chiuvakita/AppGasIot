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
fun RegisterScreen(navController: NavController) {

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Crear cuenta nueva", style = MaterialTheme.typography.headlineMedium)

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

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmacion,
            onValueChange = { confirmacion = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        if (mensajeError != null) {
            Text(mensajeError!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (correo.isBlank() || contrasena.isBlank() || confirmacion.isBlank()) {
                    mensajeError = "Debe completar todos los campos."
                } else if (contrasena != confirmacion) {
                    mensajeError = "Las contraseñas no coinciden."
                } else {
                    cargando = true
                    FirebaseAuthService.registrar(correo, contrasena) { exito, error ->
                        cargando = false
                        if (exito) {
                            navController.navigate(AppScreen.Login.ruta) {
                                popUpTo(0)
                            }
                        } else {
                            mensajeError = error ?: "Error desconocido"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Creando..." else "Registrarse")
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = {
            navController.navigate(AppScreen.Login.ruta)
        }) {
            Text("Ya tengo una cuenta, iniciar sesión")
        }
    }
}
