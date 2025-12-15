package com.example.appgasiot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appgasiot.data.repository.GasRepository
import com.example.appgasiot.navigation.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangosGasScreen(navController: NavController) {

    // Estados formulario
    var minimo by remember { mutableStateOf("") }
    var maximo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var modoEditar by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    val repo = remember { GasRepository() }

    // 🔥 TIEMPO REAL
    val configGas by repo.observarRangosGas().collectAsState()

    val actualMin = configGas?.minimo?.toString()
    val actualMax = configGas?.maximo?.toString()

    // Confirmación eliminar
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de eliminar la configuración?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    cargando = true
                    repo.eliminarRangos {
                        cargando = false
                        mensaje = if (it) "Configuración eliminada." else "Error al eliminar."
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rangos de Gas - Alerta") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(AppScreen.Home.ruta) }) {
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
                .fillMaxSize()
        ) {

            Text("Configuración actual:", style = MaterialTheme.typography.titleMedium)

            if (actualMin != null && actualMax != null) {
                Text("• Rango mínimo: $actualMin ppm")
                Text("• Rango máximo: $actualMax ppm")
            } else {
                Text("No hay configuración guardada.")
            }

            Spacer(Modifier.height(20.dp))

            if (actualMin != null && actualMax != null && !modoEditar) {

                OutlinedButton(
                    onClick = {
                        minimo = actualMin
                        maximo = actualMax
                        modoEditar = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Editar configuración") }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { mostrarConfirmacion = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Eliminar configuración") }
            }

            if (modoEditar || actualMin == null) {

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = minimo,
                    onValueChange = { minimo = it },
                    label = { Text("Rango mínimo (ppm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = maximo,
                    onValueChange = { maximo = it },
                    label = { Text("Rango máximo (ppm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        val minVal = minimo.toIntOrNull()
                        val maxVal = maximo.toIntOrNull()

                        mensaje = when {
                            minVal == null || maxVal == null ->
                                "Debe ingresar valores numéricos."

                            minVal >= maxVal ->
                                "El mínimo debe ser menor que el máximo."

                            else -> {
                                cargando = true
                                repo.guardarRangos(minVal, maxVal) {
                                    cargando = false
                                }
                                modoEditar = false
                                "Rangos guardados correctamente."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(if (cargando) "Guardando..." else "Guardar")
                }

                TextButton(
                    onClick = { modoEditar = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }
            }

            Spacer(Modifier.height(16.dp))

            mensaje?.let {
                Text(
                    it,
                    color = if (it.contains("correctamente"))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
