package com.example.appgasiot.data.repository

import com.example.appgasiot.data.firebase.FirebaseDatabaseService
import com.example.appgasiot.data.model.GasConfig

class GasRepository(
    private val dbService: FirebaseDatabaseService = FirebaseDatabaseService()
) {

    fun guardarRangos(min: Int, max: Int, callback: (Boolean) -> Unit) {
        dbService.guardarRangos(min, max, callback)
    }

    fun leerRangos(callback: (GasConfig?) -> Unit) {
        dbService.leerRangos(callback)
    }

    fun eliminarRangos(callback: (Boolean) -> Unit) {
        dbService.eliminarRangos(callback)
    }
}
