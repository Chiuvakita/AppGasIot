package com.example.appgasiot.data.model

class Event {
    data class Event(
        val tipo: String = "",
        val mensaje: String = "",
        val fecha: Long = 0
    )
}