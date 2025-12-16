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

                val min = snapshot
                    .child("rangos")
                    .child("minimo")
                    .getValue(Int::class.java) ?: 0

                val max = snapshot
                    .child("rangos")
                    .child("maximo")
                    .getValue(Int::class.java) ?: 0


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
        db.child("rangos").setValue(
            mapOf(
                "minimo" to min,
                "maximo" to max
            )
        )
    }


    fun eliminarRangos() {
        db.child("rangos").removeValue()
            .addOnSuccessListener {
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
