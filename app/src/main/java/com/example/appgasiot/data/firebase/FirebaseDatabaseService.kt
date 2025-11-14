package com.example.appgasiot.data.firebase

import com.example.appgasiot.data.model.GasConfig
import com.google.firebase.database.FirebaseDatabase

class FirebaseDatabaseService {

    private val db = FirebaseDatabase.getInstance().reference

    // GUARDAR RANGOS DE GAS
    fun guardarRangos(min: Int, max: Int, onComplete: (Boolean) -> Unit) {
        val data = mapOf("minimo" to min, "maximo" to max)

        db.child("config_gas").setValue(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // LEER RANGOS DE GAS
    fun leerRangos(onData: (GasConfig?) -> Unit) {
        db.child("config_gas").get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    onData(null)
                    return@addOnSuccessListener
                }

                val min = snap.child("minimo").value?.toString()?.toIntOrNull()
                val max = snap.child("maximo").value?.toString()?.toIntOrNull()

                if (min != null && max != null) {
                    onData(GasConfig(min, max))
                } else {
                    onData(null)
                }
            }
            .addOnFailureListener {
                onData(null)
            }
    }

    // ELIMINAR RANGOS
    fun eliminarRangos(onComplete: (Boolean) -> Unit) {
        db.child("config_gas").removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
