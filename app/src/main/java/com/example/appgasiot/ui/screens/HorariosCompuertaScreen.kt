package com.example.appgasiot.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appgasiot.navigation.AppScreen
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosCompuertaScreen(navController: NavController, modo: String?) {

    val modoSeguro = when (modo) {
        "apertura", "cierre" -> modo
        else -> "apertura"
    }

    val titulo =
        if (modoSeguro == "apertura") "Período de APERTURA"
        else "Período de CIERRE"

    val dbRoot = FirebaseDatabase.getInstance().reference.child("horarios_compuerta")
    val dbRef = dbRoot.child(modoSeguro!!)

    var inicioFechaActual by remember { mutableStateOf<String?>(null) }
    var inicioHoraActual by remember { mutableStateOf<String?>(null) }
    var finFechaActual by remember { mutableStateOf<String?>(null) }
    var finHoraActual by remember { mutableStateOf<String?>(null) }

    var fechaInicio by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }

    var modoEditar by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    // ✅ TIEMPO REAL: en vez de get(), escuchamos el nodo del modo
    DisposableEffect(modoSeguro) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                if (snap.exists()) {
                    inicioFechaActual = snap.child("inicio_fecha").value?.toString()
                    inicioHoraActual = snap.child("inicio_hora").value?.toString()
                    finFechaActual = snap.child("fin_fecha").value?.toString()
                    finHoraActual = snap.child("fin_hora").value?.toString()
                } else {
                    inicioFechaActual = null
                    inicioHoraActual = null
                    finFechaActual = null
                    finHoraActual = null
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // no cambia tu UI
            }
        }

        dbRef.addValueEventListener(listener)

        onDispose {
            dbRef.removeEventListener(listener)
        }
    }

    fun seleccionarFechaHora(onResult: (String, String) -> Unit) {
        val cal = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val fecha = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val hora = "%02d:%02d".format(hourOfDay, minute)
                        onResult(fecha, hora)
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Deseas borrar el período completo?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacion = false
                    dbRef.removeValue()
                    inicioFechaActual = null
                    inicioHoraActual = null
                    finFechaActual = null
                    finHoraActual = null
                    fechaInicio = ""
                    horaInicio = ""
                    fechaFin = ""
                    horaFin = ""
                    modoEditar = false
                    mensaje = "Período eliminado correctamente."
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
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(AppScreen.HorariosCompuertaMenu.ruta)
                    }) {
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

            Text("Configuración actual:", style = MaterialTheme.typography.titleMedium)

            if (inicioFechaActual != null && inicioHoraActual != null &&
                finFechaActual != null && finHoraActual != null
            ) {
                Text("• Inicio: $inicioFechaActual - $inicioHoraActual")
                Text("• Fin: $finFechaActual - $finHoraActual")
            } else {
                Text("No hay configuración guardada para este modo.")
            }

            Spacer(Modifier.height(20.dp))

            if (!modoEditar && inicioFechaActual != null) {

                OutlinedButton(
                    onClick = {
                        modoEditar = true
                        fechaInicio = inicioFechaActual ?: ""
                        horaInicio = inicioHoraActual ?: ""
                        fechaFin = finFechaActual ?: ""
                        horaFin = finHoraActual ?: ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Editar período") }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { mostrarConfirmacion = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Eliminar período") }

                Spacer(Modifier.height(20.dp))
            }

            if (modoEditar || inicioFechaActual == null) {

                Text("Inicio del período:", style = MaterialTheme.typography.labelLarge)

                OutlinedButton(
                    onClick = {
                        seleccionarFechaHora { fecha, hora ->
                            fechaInicio = fecha
                            horaInicio = hora
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (fechaInicio.isEmpty())
                            "Seleccionar fecha y hora de inicio"
                        else
                            "$fechaInicio $horaInicio"
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text("Fin del período:", style = MaterialTheme.typography.labelLarge)

                OutlinedButton(
                    onClick = {
                        seleccionarFechaHora { fecha, hora ->
                            fechaFin = fecha
                            horaFin = hora
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (fechaFin.isEmpty())
                            "Seleccionar fecha y hora de fin"
                        else
                            "$fechaFin $horaFin"
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (fechaInicio.isBlank() || horaInicio.isBlank() ||
                            fechaFin.isBlank() || horaFin.isBlank()
                        ) {
                            mensaje = "Debe seleccionar todas las fechas y horas."
                            return@Button
                        }

                        val ini = try { sdf.parse("$fechaInicio $horaInicio")?.time ?: 0L } catch (e: Exception) { 0L }
                        val fin = try { sdf.parse("$fechaFin $horaFin")?.time ?: 0L } catch (e: Exception) { 0L }

                        if (ini == 0L || fin == 0L) {
                            mensaje = "Formato de fecha/hora inválido."
                            return@Button
                        }

                        if (ini >= fin) {
                            mensaje = "La fecha/hora de inicio debe ser menor que la de fin."
                            return@Button
                        }

                        val otraClave = if (modoSeguro == "apertura") "cierre" else "apertura"

                        dbRoot.child(otraClave).get().addOnSuccessListener { otherSnap ->
                            if (otherSnap.exists()) {
                                val oIniFecha = otherSnap.child("inicio_fecha").value?.toString()
                                val oIniHora = otherSnap.child("inicio_hora").value?.toString()
                                val oFinFecha = otherSnap.child("fin_fecha").value?.toString()
                                val oFinHora = otherSnap.child("fin_hora").value?.toString()

                                if (!oIniFecha.isNullOrBlank() && !oIniHora.isNullOrBlank()
                                    && !oFinFecha.isNullOrBlank() && !oFinHora.isNullOrBlank()
                                ) {
                                    val oIni = sdf.parse("$oIniFecha $oIniHora")?.time ?: 0L
                                    val oFin = sdf.parse("$oFinFecha $oFinHora")?.time ?: 0L

                                    val inicioMax = maxOf(ini, oIni)
                                    val finMin = minOf(fin, oFin)

                                    if (inicioMax < finMin) {
                                        mensaje =
                                            "El período se cruza con el período de ${if (modoSeguro == "apertura") "CIERRE" else "APERTURA"}."
                                        return@addOnSuccessListener
                                    }
                                }
                            }

                            val data = mapOf(
                                "inicio_fecha" to fechaInicio,
                                "inicio_hora" to horaInicio,
                                "fin_fecha" to fechaFin,
                                "fin_hora" to horaFin
                            )

                            dbRef.setValue(data)
                                .addOnSuccessListener {
                                    inicioFechaActual = fechaInicio
                                    inicioHoraActual = horaInicio
                                    finFechaActual = fechaFin
                                    finHoraActual = horaFin
                                    modoEditar = false
                                    mensaje = "Período guardado correctamente."
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Guardar período")
                }

                Spacer(Modifier.height(10.dp))

                if (modoEditar) {
                    TextButton(
                        onClick = { modoEditar = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar")
                    }
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
