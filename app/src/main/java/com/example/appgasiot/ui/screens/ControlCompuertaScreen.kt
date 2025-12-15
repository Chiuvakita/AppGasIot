package com.example.appgasiot.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appgasiot.data.State.ControlCompuertaState
import com.example.appgasiot.data.firebase.FirebaseControlCompuertaService
import com.example.appgasiot.navigation.AppScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlCompuertaScreen(navController: NavController) {

    val service = remember { FirebaseControlCompuertaService() }

    val modo by ControlCompuertaState.modo.collectAsState()
    val estado by ControlCompuertaState.estado.collectAsState()
    val rangoMin by ControlCompuertaState.rangoMin.collectAsState()
    val rangoMax by ControlCompuertaState.rangoMax.collectAsState()

    var minInput by remember { mutableStateOf(rangoMin.toString()) }
    var maxInput by remember { mutableStateOf(rangoMax.toString()) }
    var mostrarRangos by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Compuerta") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(AppScreen.Home.ruta)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ===============================
            // MODO DE OPERACIÓN
            // ===============================
            Text("Modo de operación", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                AssistChip(
                    onClick = { service.cambiarModo("automatico") },
                    label = { Text("Automático") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor =
                            if (modo == "automatico")
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                AssistChip(
                    onClick = { service.cambiarModo("manual") },
                    label = { Text("Manual") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor =
                            if (modo == "manual")
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Divider()

            // ===============================
            // MODO AUTOMÁTICO
            // ===============================
            if (modo == "automatico") {

                Text("Modo automático", style = MaterialTheme.typography.titleMedium)

                Text(
                    "La compuerta se controla automáticamente según el nivel de gas:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    "• Si el gas supera el máximo → la compuerta se cierra\n" +
                            "• Si el gas baja del mínimo → la compuerta se abre",
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarRangos = !mostrarRangos },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text("Rangos configurados")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$rangoMin ppm  →  $rangoMax ppm",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (mostrarRangos)
                                "Tocar para cerrar"
                            else
                                "Tocar para editar rangos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (mostrarRangos) {

                    OutlinedTextField(
                        value = minInput,
                        onValueChange = { if (it.all(Char::isDigit)) minInput = it },
                        label = { Text("Gas mínimo (abre compuerta)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = maxInput,
                        onValueChange = { if (it.all(Char::isDigit)) maxInput = it },
                        label = { Text("Gas máximo (cierra compuerta)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            service.guardarRangos(
                                minInput.toIntOrNull() ?: rangoMin,
                                maxInput.toIntOrNull() ?: rangoMax
                            )
                            mostrarRangos = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar rangos")
                    }

                    OutlinedButton(
                        onClick = {
                            service.eliminarRangos()
                            mostrarRangos = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar configuración automática")
                    }
                }
            }

            // ===============================
            // MODO MANUAL
            // ===============================
            if (modo == "manual") {

                Text("Control manual", style = MaterialTheme.typography.titleMedium)

                Text(
                    "Estado actual de la compuerta: ${estado.uppercase()}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    Button(
                        onClick = { service.cambiarEstado("abierto") }
                    ) {
                        Text("Abrir")
                    }

                    Button(
                        onClick = { service.cambiarEstado("cerrado") }
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}
