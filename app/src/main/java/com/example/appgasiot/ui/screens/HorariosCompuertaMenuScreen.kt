package com.example.appgasiot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appgasiot.navigation.AppScreen
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosCompuertaMenuScreen(navController: NavController) {

    val dbRef = FirebaseDatabase.getInstance().reference.child("horarios_compuerta")

    var apertura by remember { mutableStateOf<Map<String, String>?>(null) }
    var cierre by remember { mutableStateOf<Map<String, String>?>(null) }

    // ✅ Tick para que el texto "Estado" se refresque solo (sin cambiar diseño)
    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            tick = System.currentTimeMillis()
            kotlinx.coroutines.delay(30_000) // cada 30s
        }
    }

    // 🔹 Calcula estado y tiempo restante según fecha/hora y tipo (apertura/cierre)
    fun calcularEstado(data: Map<String, String>?, tipo: String): String {
        if (data == null) return "Sin horario definido"

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val inicioMs = sdf.parse("${data["inicio_fecha"]} ${data["inicio_hora"]}")?.time ?: return ""
        val finMs = sdf.parse("${data["fin_fecha"]} ${data["fin_hora"]}")?.time ?: return ""
        val ahora = System.currentTimeMillis()

        val etiquetaAbiertoCerrado =
            if (tipo == "apertura") "ABIERTO" else "CERRADO"

        return when {
            ahora < inicioMs -> {
                val dif = inicioMs - ahora
                val horas = (dif / 3_600_000)
                val mins = (dif / 60_000) % 60
                "Programado – quedará $etiquetaAbiertoCerrado en $horas h $mins min"
            }
            ahora in inicioMs..finMs -> {
                val dif = finMs - ahora
                val horas = (dif / 3_600_000)
                val mins = (dif / 60_000) % 60
                "En ejecución – compuerta $etiquetaAbiertoCerrado, termina en $horas h $mins min"
            }
            else -> "Finalizado – período $etiquetaAbiertoCerrado ya terminó"
        }
    }

    // ✅ TIEMPO REAL: escucha apertura y cierre
    DisposableEffect(Unit) {
        val aperturaListener = object : ValueEventListener {
            override fun onDataChange(it: DataSnapshot) {
                apertura =
                    if (it.exists()) {
                        mapOf(
                            "inicio_fecha" to (it.child("inicio_fecha").value?.toString() ?: ""),
                            "inicio_hora" to (it.child("inicio_hora").value?.toString() ?: ""),
                            "fin_fecha" to (it.child("fin_fecha").value?.toString() ?: ""),
                            "fin_hora" to (it.child("fin_hora").value?.toString() ?: "")
                        )
                    } else null
            }

            override fun onCancelled(error: DatabaseError) {
                // no cambia tu UI, solo evita crash
            }
        }

        val cierreListener = object : ValueEventListener {
            override fun onDataChange(it: DataSnapshot) {
                cierre =
                    if (it.exists()) {
                        mapOf(
                            "inicio_fecha" to (it.child("inicio_fecha").value?.toString() ?: ""),
                            "inicio_hora" to (it.child("inicio_hora").value?.toString() ?: ""),
                            "fin_fecha" to (it.child("fin_fecha").value?.toString() ?: ""),
                            "fin_hora" to (it.child("fin_hora").value?.toString() ?: "")
                        )
                    } else null
            }

            override fun onCancelled(error: DatabaseError) {
                // no cambia tu UI
            }
        }

        dbRef.child("apertura").addValueEventListener(aperturaListener)
        dbRef.child("cierre").addValueEventListener(cierreListener)

        onDispose {
            dbRef.child("apertura").removeEventListener(aperturaListener)
            dbRef.child("cierre").removeEventListener(cierreListener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Horarios de compuerta") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(AppScreen.Home.ruta) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            verticalArrangement = Arrangement.Top
        ) {

            Text(
                "Selecciona qué deseas configurar:",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    navController.navigate(
                        AppScreen.HorariosCompuerta.crearRuta("apertura")
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Configurar APERTURA")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate(
                        AppScreen.HorariosCompuerta.crearRuta("cierre")
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Configurar CIERRE")
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Período de APERTURA",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            if (apertura == null) {
                Text("Sin configuración de apertura.")
            } else {
                val a = apertura!!
                Text("Inicio: ${a["inicio_fecha"]} - ${a["inicio_hora"]}")
                Text("Fin: ${a["fin_fecha"]} - ${a["fin_hora"]}")
                Text("Estado: ${calcularEstado(a, "apertura")}")

                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            navController.navigate(
                                AppScreen.HorariosCompuerta.crearRuta("apertura")
                            )
                        }
                    ) { Text("Editar") }

                    TextButton(
                        onClick = {
                            dbRef.child("apertura").removeValue()
                            // no seteo apertura = null a mano; el listener lo hará en tiempo real
                        }
                    ) { Text("Eliminar") }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Período de CIERRE",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            if (cierre == null) {
                Text("Sin configuración de cierre.")
            } else {
                val c = cierre!!
                Text("Inicio: ${c["inicio_fecha"]} - ${c["inicio_hora"]}")
                Text("Fin: ${c["fin_fecha"]} - ${c["fin_hora"]}")
                Text("Estado: ${calcularEstado(c, "cierre")}")

                Spacer(Modifier.height(8.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            navController.navigate(
                                AppScreen.HorariosCompuerta.crearRuta("cierre")
                            )
                        }
                    ) { Text("Editar") }

                    TextButton(
                        onClick = {
                            dbRef.child("cierre").removeValue()
                            // listener lo actualiza en tiempo real
                        }
                    ) { Text("Eliminar") }
                }
            }
        }
    }
}
