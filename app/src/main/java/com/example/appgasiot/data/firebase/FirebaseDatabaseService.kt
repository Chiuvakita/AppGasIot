package com.example.appgasiot.data.firebase

import android.util.Log
import com.example.appgasiot.data.state.GasState
import com.google.firebase.database.*

class FirebaseDatabaseService {

    private val db = FirebaseDatabase.getInstance().reference

    init {
        escucharRangosGas()
    }

    private fun escucharRangosGas() {
        db.child("config_gas")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("REALTIME", "Cambio detectado: ${snapshot.value}")

                    val min = snapshot.child("minimo").getValue(Int::class.java)
                    val max = snapshot.child("maximo").getValue(Int::class.java)

                    if (min != null && max != null) {
                        GasState.actualizar(min, max)
                    } else {
                        GasState.limpiar()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("REALTIME", "Error Firebase: ${error.message}")
                }
            })
    }

    fun guardarRangos(min: Int, max: Int, callback: (Boolean) -> Unit) {
        val data = mapOf(
            "minimo" to min,
            "maximo" to max
        )

        db.child("config_gas")
            .setValue(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun eliminarRangos(callback: (Boolean) -> Unit) {
        db.child("config_gas")
            .removeValue()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
