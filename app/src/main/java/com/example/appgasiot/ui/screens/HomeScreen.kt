package com.example.appgasiot.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appgasiot.navigation.AppScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val db = FirebaseDatabase.getInstance().reference

    var estadoCompuerta by remember { mutableStateOf("Cargando...") }
    var ultimaLectura by remember { mutableStateOf("Cargando...") }
    var ultimoEvento by remember { mutableStateOf("Cargando...") }

    // ==============================
    // 🔥 LISTENERS EN TIEMPO REAL
    // ==============================
    DisposableEffect(Unit) {

        // ---- ESTADO COMPUERTA ----
        val estadoListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val est = snapshot.value?.toString()
                estadoCompuerta = when (est) {
                    "abierto" -> "Abierta"
                    "cerrado" -> "Cerrada"
                    else -> "Sin configuración"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        db.child("control_compuerta").child("estado")
            .addValueEventListener(estadoListener)

        // ---- ÚLTIMA LECTURA ----
        val lecturaListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.firstOrNull()?.let { child ->
                    val valor = child.child("valor").value?.toString()
                    val fecha = child.child("fecha").value?.toString()
                    val hora = child.child("hora").value?.toString()
                    ultimaLectura = "$valor ppm • $fecha $hora"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        db.child("historial_lecturas")
            .limitToLast(1)
            .addValueEventListener(lecturaListener)

        // ---- ÚLTIMO EVENTO CRÍTICO ----
        val eventoListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.firstOrNull()?.let { child ->
                    val desc = child.child("descripcion").value?.toString()
                    val valor = child.child("valor").value?.toString()
                    val fecha = child.child("fecha").value?.toString()
                    val hora = child.child("hora").value?.toString()
                    ultimoEvento = "$desc • $valor ppm • $fecha $hora"
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        db.child("eventos_criticos")
            .limitToLast(1)
            .addValueEventListener(eventoListener)

        // 🔥 LIMPIEZA
        onDispose {
            db.child("control_compuerta").child("estado")
                .removeEventListener(estadoListener)

            db.child("historial_lecturas")
                .removeEventListener(lecturaListener)

            db.child("eventos_criticos")
                .removeEventListener(eventoListener)
        }
    }

    // ==============================
    // UI
    // ==============================
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {

                Spacer(Modifier.height(40.dp))
                Text(
                    "Menú IoT",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(20.dp)
                )

                DrawerItem("Rangos de gas - Alerta") {
                    navController.navigate(AppScreen.RangosGas.ruta)
                }

                DrawerItem("Horarios de compuerta") {
                    navController.navigate(AppScreen.HorariosCompuertaMenu.ruta)
                }

                DrawerItem("Eventos críticos") {
                    navController.navigate(AppScreen.EventosCriticos.ruta)
                }

                DrawerItem("Historial de lecturas") {
                    navController.navigate(AppScreen.HistorialLecturas.ruta)
                }

                DrawerItem("Control de compuerta") {
                    navController.navigate(AppScreen.ControlCompuerta.ruta)
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider()

                DrawerItem("Cerrar sesión", icon = Icons.AutoMirrored.Filled.Logout) {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(AppScreen.Login.ruta) {
                        popUpTo(0)
                    }
                }
            }
        }
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Panel IoT") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->

            Column(
                Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .fillMaxSize()
            ) {

                Text("Bienvenido", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))

                DashboardCard("Estado de la compuerta", estadoCompuerta)
                Spacer(Modifier.height(14.dp))

                DashboardCard("Última lectura de gas", ultimaLectura)
                Spacer(Modifier.height(14.dp))

                DashboardCard("Último evento crítico", ultimoEvento)
            }
        }
    }
}

@Composable
private fun DashboardCard(titulo: String, contenido: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(contenido)
        }
    }
}

@Composable
fun DrawerItem(
    texto: String,
    icon: ImageVector = Icons.AutoMirrored.Filled.ListAlt,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Icon(icon, contentDescription = texto)
        Spacer(Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodyLarge)
    }
}
