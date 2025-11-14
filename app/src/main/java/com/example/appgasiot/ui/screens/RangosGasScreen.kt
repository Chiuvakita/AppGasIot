package com.example.appgasiot.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appgasiot.navigation.AppScreen
import com.example.appgasiot.data.repository.GasRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangosGasScreen(navController: NavController) {

    // Estados de UI
    var minimo by remember { mutableStateOf("") }
    var maximo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var modoEditar by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    // Config actual
    var actualMin by remember { mutableStateOf<String?>(null) }
    var actualMax by remember { mutableStateOf<String?>(null) }

    val repo = GasRepository()

    // Cargar valores actuales desde Firebase
    LaunchedEffect(Unit) {
        repo.leerRangos { config ->
            actualMin = config?.minimo?.toString()
            actualMax = config?.maximo?.toString()
        }
    }

    // Diálogo de confirmación para eliminar
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar la configuración?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    cargando = true

                    repo.eliminarRangos { ok ->
                        cargando = false
                        if (ok) {
                            actualMin = null
                            actualMax = null
                            minimo = ""
                            maximo = ""
                            modoEditar = false
                            mensaje = "Configuración eliminada correctamente."
                        } else {
                            mensaje = "Error al eliminar."
                        }
                    }
                }) {
                    Text("Eliminar")
                }
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
                title = { Text("Rangos de Gas") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(AppScreen.Home.ruta) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {

            Text("Configuración actual:", style = MaterialTheme.typography.titleMedium)

            if (actualMin != null && actualMax != null) {
                Text("• Rango mínimo: ${actualMin} ppm")
                Text("• Rango máximo: ${actualMax} ppm")
            } else {
                Text("No hay configuración guardada.")
            }

            Spacer(Modifier.height(20.dp))

            // Botón EDITAR
            if (actualMin != null && actualMax != null && !modoEditar) {
                OutlinedButton(
                    onClick = {
                        minimo = actualMin ?: ""
                        maximo = actualMax ?: ""
                        modoEditar = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Editar configuración") }

                Spacer(Modifier.height(12.dp))

                // Botón ELIMINAR
                OutlinedButton(
                    onClick = { mostrarConfirmacion = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Eliminar configuración") }

                Spacer(Modifier.height(20.dp))
            }

            // FORMULARIO
            if (modoEditar || actualMin == null) {

                OutlinedTextField(
                    value = minimo,
                    onValueChange = { minimo = it },
                    label = { Text("Rango mínimo (ppm)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = maximo,
                    onValueChange = { maximo = it },
                    label = { Text("Rango máximo (ppm)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // BOTÓN GUARDAR
                Button(
                    onClick = {
                        val minVal = minimo.toIntOrNull()
                        val maxVal = maximo.toIntOrNull()

                        when {
                            minVal == null || maxVal == null ->
                                mensaje = "Debe ingresar valores numéricos."

                            minVal >= maxVal ->
                                mensaje = "El mínimo debe ser menor que el máximo."

                            else -> {
                                cargando = true
                                repo.guardarRangos(minVal, maxVal) { ok ->
                                    cargando = false

                                    if (ok) {
                                        actualMin = minVal.toString()
                                        actualMax = maxVal.toString()
                                        modoEditar = false
                                        mensaje = "Rangos guardados correctamente."
                                    } else {
                                        mensaje = "Error al guardar."
                                    }
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
                    text = it,
                    color = if (it.contains("correctamente"))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
