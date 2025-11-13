package com.example.appgasiot.data.firebase

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun iniciarSesion(correo: String, contrasena: String, resultado: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(correo, contrasena)
            .addOnSuccessListener {
                resultado(true, null)
            }
            .addOnFailureListener { error ->
                resultado(false, error.message)
            }
    }
    fun registrar(correo: String, contrasena: String, resultado: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnSuccessListener { resultado(true, null) }
            .addOnFailureListener { error -> resultado(false, error.message) }
    }
}

