package com.example.appgasiot.data.firebase

import android.util.Log
import com.example.appgasiot.data.State.ControlCompuertaState
import com.google.firebase.database.*

class FirebaseControlCompuertaService {

    private val db = FirebaseDatabase.getInstance().reference.child("control_compuerta")
    private var listener: ValueEventListener? = null

    init {
        escucharTiempoReal()
    }

    private fun escucharTiempoReal() {
        if (listener != null) return

        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val modo = snapshot.child("modo").getValue(String::class.java) ?: "manual"
                val estado = snapshot.child("estado").getValue(String::class.java) ?: "cerrado"

                val min = snapshot.child("rango_min").getValue(Int::class.java) ?: 0
                val max = snapshot.child("rango_max").getValue(Int::class.java) ?: 0

                ControlCompuertaState.setModo(modo)
                ControlCompuertaState.setEstado(estado)
                ControlCompuertaState.setRangos(min, max)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CTRL_COMPUERTA", "Firebase error: ${error.message}")
            }
        }

        db.addValueEventListener(listener!!)
    }

    fun cambiarModo(modo: String) {
        db.child("modo").setValue(modo)
    }

    fun cambiarEstado(estado: String) {
        db.child("estado").setValue(estado)
    }

    fun guardarRangos(min: Int, max: Int) {
        val data = mapOf(
            "rango_min" to min,
            "rango_max" to max
        )
        db.updateChildren(data)
    }

    fun eliminarRangos() {
        val updates = mapOf<String, Any?>(
            "rango_min" to null,
            "rango_max" to null
        )
        db.updateChildren(updates).addOnSuccessListener {
            ControlCompuertaState.clearRangos()
        }
    }

    fun detener() {
        listener?.let {
            db.removeEventListener(it)
            listener = null
        }
    }
}
