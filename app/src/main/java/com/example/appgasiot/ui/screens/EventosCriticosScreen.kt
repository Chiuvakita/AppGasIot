package com.example.appgasiot.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.appgasiot.data.model.EventoCritico
import com.example.appgasiot.navigation.AppScreen
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import java.util.*




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosCriticosScreen(navController: NavController) {

    val db = FirebaseDatabase.getInstance().reference.child("eventos_criticos")

    var listaEventos by remember { mutableStateOf(listOf<EventoCritico>()) }

    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var valorMin by remember { mutableStateOf("") }
    var valorMax by remember { mutableStateOf("") }

    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var eventoAEliminar by remember { mutableStateOf<EventoCritico?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Carga de datos
    LaunchedEffect(Unit) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = mutableListOf<EventoCritico>()
                for (child in snapshot.children) {
                    temp.add(
                        EventoCritico(
                            id = child.key ?: "",
                            descripcion = child.child("descripcion").value?.toString() ?: "",
                            fecha = child.child("fecha").value?.toString() ?: "",
                            hora = child.child("hora").value?.toString() ?: "",
                            valor = child.child("valor").value?.toString()?.toIntOrNull() ?: 0
                        )
                    )
                }
                listaEventos = temp.sortedByDescending { it.fecha + it.hora }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Confirmación de eliminación
    if (mostrarConfirmacion && eventoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Eliminar evento") },
            text = { Text("¿Seguro que deseas eliminar este evento crítico?") },
            confirmButton = {
                TextButton(onClick = {
                    db.child(eventoAEliminar!!.id).removeValue()
                    mostrarConfirmacion = false
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
                title = { Text("Eventos críticos") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(AppScreen.Home.ruta) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")

                    }
                }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // Filtros
            Text("Filtrar por fecha", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth()) {

                // Fecha inicio
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                fechaInicio = "%04d-%02d-%02d".format(y, m + 1, d)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (fechaInicio.isEmpty()) "Fecha inicio" else fechaInicio)
                }

                Spacer(Modifier.width(10.dp))

                // Fecha fin
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                fechaFin = "%04d-%02d-%02d".format(y, m + 1, d)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (fechaFin.isEmpty()) "Fecha fin" else fechaFin)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Filtro por valor
            Text("Filtrar por valor (ppm)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth()) {

                OutlinedTextField(
                    value = valorMin,
                    onValueChange = { valorMin = it },
                    label = { Text("Mínimo") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(12.dp))

                OutlinedTextField(
                    value = valorMax,
                    onValueChange = { valorMax = it },
                    label = { Text("Máximo") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Botón mostrar solo cuando hay filtros activos
            val hayFiltros =
                fechaInicio.isNotEmpty() ||
                        fechaFin.isNotEmpty() ||
                        valorMin.isNotEmpty() ||
                        valorMax.isNotEmpty()

            if (hayFiltros) {
                TextButton(
                    onClick = {
                        fechaInicio = ""
                        fechaFin = ""
                        valorMin = ""
                        valorMax = ""
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Limpiar filtros")
                }
            }

            Spacer(Modifier.height(10.dp))

            // Filtrado de datos
            val filtrados = listaEventos.filter { ev ->

                val cumpleFechaInicio =
                    fechaInicio.isBlank() || ev.fecha >= fechaInicio

                val cumpleFechaFin =
                    fechaFin.isBlank() || ev.fecha <= fechaFin

                val minV = valorMin.toIntOrNull() ?: Int.MIN_VALUE
                val maxV = valorMax.toIntOrNull() ?: Int.MAX_VALUE
                val cumpleValor = ev.valor in minV..maxV

                cumpleFechaInicio && cumpleFechaFin && cumpleValor
            }

            // Lista
            LazyColumn {
                items(filtrados) { ev ->

                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {

                        Column(Modifier.padding(14.dp)) {

                            Text(
                                text = ev.descripcion,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))
                            Text("Fecha: ${ev.fecha}")
                            Text("Hora: ${ev.hora}")
                            Text("Valor gas: ${ev.valor} ppm")

                            Spacer(Modifier.height(6.dp))

                            IconButton(
                                onClick = {
                                    eventoAEliminar = ev
                                    mostrarConfirmacion = true
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
