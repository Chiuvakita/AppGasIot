package com.example.appgasiot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangosGasScreen(navController: NavController) {

    var minimo by remember { mutableStateOf("") }
    var maximo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    val db = FirebaseDatabase.getInstance().reference

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rangos de Gas") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {

            Text(
                "Establecer rangos de gas",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(20.dp))

            // RANGO MINIMO
            OutlinedTextField(
                value = minimo,
                onValueChange = { minimo = it },
                label = { Text("Rango mínimo (ppm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )




            Spacer(Modifier.height(16.dp))

            // RANGO MAXIMO
            OutlinedTextField(
                value = maximo,
                onValueChange = { maximo = it },
                label = { Text("Rango máximo (ppm)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val minVal = minimo.toIntOrNull()
                    val maxVal = maximo.toIntOrNull()

                    // VALIDACIONES
                    when {
                        minVal == null || maxVal == null ->
                            mensaje = "Debe ingresar valores numéricos."

                        minVal >= maxVal ->
                            mensaje = "El mínimo debe ser menor que el máximo."

                        else -> {
                            cargando = true
                            val data = mapOf(
                                "minimo" to minVal,
                                "maximo" to maxVal
                            )

                            db.child("config_gas").setValue(data)
                                .addOnSuccessListener {
                                    cargando = false
                                    mensaje = "Rangos guardados correctamente."
                                }
                                .addOnFailureListener {
                                    cargando = false
                                    mensaje = "Error al guardar."
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (cargando) "Guardando..." else "Guardar")
            }

            Spacer(Modifier.height(16.dp))

            mensaje?.let {
                Text(
                    text = it,
                    color = if (it.contains("correctamente")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
