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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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

                // ⭐ SOLO LAS 4 OPCIONES QUE TE PIDIERON

                DrawerItem("Rangos de gas") {
                    navController.navigate(AppScreen.RangosGas.ruta)
                }

                DrawerItem("Horarios de compuerta") {
                    // navController.navigate(AppScreen.Horarios.ruta)
                }

                DrawerItem("Eventos críticos") {
                    // navController.navigate(AppScreen.Eventos.ruta)
                }

                DrawerItem("Historial de lecturas") {
                    // navController.navigate(AppScreen.Historial.ruta)
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
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Bienvenido a la App IoT",
                    style = MaterialTheme.typography.headlineMedium
                )
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
