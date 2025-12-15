package com.example.appgasiot.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appgasiot.data.model.LecturaGas
import com.example.appgasiot.navigation.AppScreen
import com.google.firebase.database.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialLecturasScreen(navController: NavController) {

    val dbRef = FirebaseDatabase.getInstance().reference.child("historial_lecturas")

    var lecturas by remember { mutableStateOf(listOf<LecturaGas>()) }
    var cargando by remember { mutableStateOf(true) }

    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var valorMin by remember { mutableStateOf("") }
    var valorMax by remember { mutableStateOf("") }
    var compuertaFiltro by remember { mutableStateOf("Todos") }

    val context = LocalContext.current

    val mapaFiltro = mapOf(
        "Todos" to "",
        "Abierto" to "abierto",
        "Cerrado" to "cerrado"
    )

    // 🔴 TIEMPO REAL REAL (listener bien gestionado)
    DisposableEffect(Unit) {

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = mutableListOf<LecturaGas>()

                for (item in snapshot.children) {
                    temp.add(
                        LecturaGas(
                            valor = item.child("valor").value?.toString() ?: "",
                            fecha = item.child("fecha").value?.toString() ?: "",
                            hora = item.child("hora").value?.toString() ?: "",
                            compuerta = item.child("compuerta").value?.toString() ?: ""
                        )
                    )
                }

                lecturas = temp.reversed()
                cargando = false
            }

            override fun onCancelled(error: DatabaseError) {
                cargando = false
            }
        }

        dbRef.addValueEventListener(listener)

        onDispose {
            dbRef.removeEventListener(listener)
        }
    }

    val hayFiltros =
        fechaInicio.isNotEmpty() ||
                fechaFin.isNotEmpty() ||
                valorMin.isNotEmpty() ||
                valorMax.isNotEmpty() ||
                compuertaFiltro != "Todos"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de lecturas") },
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
            Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
        ) {

            Text(
                "Todas las mediciones del sensor",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(20.dp))

            if (cargando) {
                CircularProgressIndicator()
                return@Column
            }

            if (lecturas.isEmpty()) {
                Text("No hay lecturas registradas.")
                return@Column
            }

            // ---------- FILTROS FECHA ----------
            Text("Filtrar por fecha")
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {

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

            Spacer(Modifier.height(16.dp))

            // ---------- FILTRO VALOR ----------
            Text("Filtrar por valor (ppm)")
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {

                OutlinedTextField(
                    value = valorMin,
                    onValueChange = { if (it.all { c -> c.isDigit() }) valorMin = it },
                    label = { Text("Mínimo") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(Modifier.width(12.dp))

                OutlinedTextField(
                    value = valorMax,
                    onValueChange = { if (it.all { c -> c.isDigit() }) valorMax = it },
                    label = { Text("Máximo") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---------- FILTRO COMPUERTA ----------
            Text("Estado de la compuerta")
            Spacer(Modifier.height(8.dp))

            Row {
                listOf("Todos", "Abierto", "Cerrado").forEach { estado ->
                    AssistChip(
                        onClick = { compuertaFiltro = estado },
                        label = { Text(estado) },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor =
                                if (compuertaFiltro == estado)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            if (hayFiltros) {
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        fechaInicio = ""
                        fechaFin = ""
                        valorMin = ""
                        valorMax = ""
                        compuertaFiltro = "Todos"
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Limpiar filtros")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------- LISTA ----------
            val filtrados = lecturas.filter { lec ->
                val cumpleFechaInicio = fechaInicio.isBlank() || lec.fecha >= fechaInicio
                val cumpleFechaFin = fechaFin.isBlank() || lec.fecha <= fechaFin

                val valorReal = lec.valor.toIntOrNull() ?: 0
                val minV = valorMin.toIntOrNull() ?: Int.MIN_VALUE
                val maxV = valorMax.toIntOrNull() ?: Int.MAX_VALUE
                val cumpleValor = valorReal in minV..maxV

                val filtroReal = mapaFiltro[compuertaFiltro] ?: ""
                val cumpleCompuerta =
                    filtroReal.isEmpty() || lec.compuerta.equals(filtroReal, true)

                cumpleFechaInicio && cumpleFechaFin && cumpleValor && cumpleCompuerta
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtrados) { lectura ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Valor: ${lectura.valor} ppm")
                            Text("Fecha: ${lectura.fecha}  ${lectura.hora}")
                            if (lectura.compuerta.isNotEmpty()) {
                                Text("Compuerta: ${lectura.compuerta.uppercase()}")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    dbRef.removeValue()
                    lecturas = emptyList()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar historial completo")
            }
        }
    }
}
