package com.example.appgasiot.data.repository

import com.example.appgasiot.data.firebase.FirebaseDatabaseService
import com.example.appgasiot.data.state.GasState

class GasRepository {

    // Se inicializa UNA vez y deja el listener activo
    private val firebaseService = FirebaseDatabaseService()

    fun observarRangosGas() = GasState.configGas

    fun guardarRangos(min: Int, max: Int, callback: (Boolean) -> Unit) {
        firebaseService.guardarRangos(min, max, callback)
    }

    fun eliminarRangos(callback: (Boolean) -> Unit) {
        firebaseService.eliminarRangos(callback)
    }
}
