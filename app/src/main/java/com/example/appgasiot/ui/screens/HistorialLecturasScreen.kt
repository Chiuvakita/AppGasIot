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

    val dbRef = FirebaseDatabase.getInstance()
        .reference
        .child("historial_lecturas")
        .limitToLast(500)

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

            // ---------- FILTROS ----------
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

            Text("Filtrar por valor (ppm)")
            Spacer(Modifier.height(8.dp))

            Row {

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

            Text("Estado de la compuerta")
            Spacer(Modifier.height(8.dp))

            Row {
                listOf("Todos", "Abierto", "Cerrado").forEach { estado ->
                    AssistChip(
                        onClick = { compuertaFiltro = estado },
                        label = { Text(estado) },
                        modifier = Modifier.padding(end = 8.dp)
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

            // ---------- FILTRADO ----------
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

            // ---------- TIMELINE ----------
            val lecturasAgrupadas = filtrados
                .groupBy { it.fecha }
                .toSortedMap(compareByDescending { it })

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {

                lecturasAgrupadas.forEach { (fecha, lecturasDia) ->

                    item {
                        Text(
                            text = fecha,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(lecturasDia.sortedByDescending { it.hora }) { lectura ->
                        TimelineLecturaItem(lectura)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    dbRef.ref.removeValue()
                    lecturas = emptyList()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar historial completo")
            }
        }
    }
}

@Composable
fun TimelineLecturaItem(lectura: LecturaGas) {

    val colorEstado = when (lectura.compuerta.lowercase()) {
        "abierto" -> MaterialTheme.colorScheme.primary
        "cerrado" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(22.dp)
        ) {
            Surface(
                color = colorEstado,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(10.dp)
            ) {}
            Spacer(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(14.dp)) {

                Text(
                    text = "${lectura.hora} • ${lectura.valor} ppm",
                    style = MaterialTheme.typography.bodyLarge
                )

                if (lectura.compuerta.isNotBlank()) {
                    Text(
                        text = lectura.compuerta.uppercase(),
                        color = colorEstado,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
