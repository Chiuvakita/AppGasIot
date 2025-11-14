package com.example.appgasiot.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ----------------------------------
    // LECTURA DE DATOS DESDE FIREBASE
    // ----------------------------------

    val db = FirebaseDatabase.getInstance().reference

    var estadoCompuerta by remember { mutableStateOf("Cargando...") }
    var ultimaLectura by remember { mutableStateOf<String?>(null) }
    var ultimoEvento by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {

        // Estado compuerta
        db.child("horarios_compuerta").get().addOnSuccessListener { snap ->
            val est = snap.child("estado").value?.toString()
            estadoCompuerta = when (est) {
                "abierto" -> "Abierta"
                "cerrado" -> "Cerrada"
                else -> "Sin configuración"
            }
        }

        // Última lectura
        db.child("historial_lecturas").limitToLast(1).get()
            .addOnSuccessListener { snap ->
                for (child in snap.children) {
                    val valor = child.child("valor").value?.toString()
                    val fecha = child.child("fecha").value?.toString()
                    val hora = child.child("hora").value?.toString()
                    ultimaLectura = "$valor ppm • $fecha $hora"
                }
            }

        // Último evento crítico
        db.child("eventos_criticos").limitToLast(1).get()
            .addOnSuccessListener { snap ->
                for (child in snap.children) {
                    val desc = child.child("descripcion").value?.toString()
                    val fecha = child.child("fecha").value?.toString()
                    val hora = child.child("hora").value?.toString()
                    val valor = child.child("valor").value?.toString()
                    ultimoEvento = "$desc • $valor ppm • $fecha $hora"
                }
            }
    }


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

                Spacer(Modifier.height(10.dp))

                DrawerItem("Rangos de gas") {
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
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {

                Text(
                    "Bienvenido",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(20.dp))

                // TARJETA: ESTADO COMPUERTA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Estado de la compuerta", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(estadoCompuerta)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // TARJETA: ÚLTIMA LECTURA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Última lectura de gas", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(ultimaLectura ?: "No hay datos")
                    }
                }

                Spacer(Modifier.height(14.dp))

                // TARJETA: ÚLTIMO EVENTO CRÍTICO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Último evento crítico", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(ultimoEvento ?: "No hay eventos críticos")
                    }
                }
            }
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
