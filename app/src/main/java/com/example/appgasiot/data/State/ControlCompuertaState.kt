package com.example.appgasiot.data.State

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ControlCompuertaState {

    private val _modo = MutableStateFlow("manual")
    val modo = _modo.asStateFlow()

    private val _estado = MutableStateFlow("cerrado")
    val estado = _estado.asStateFlow()

    private val _rangoMin = MutableStateFlow(0)
    val rangoMin = _rangoMin.asStateFlow()

    private val _rangoMax = MutableStateFlow(0)
    val rangoMax = _rangoMax.asStateFlow()

    fun setModo(v: String) { _modo.value = v }
    fun setEstado(v: String) { _estado.value = v }

    fun setRangos(min: Int, max: Int) {
        _rangoMin.value = min
        _rangoMax.value = max
    }

    // ✅ para que el botón "Eliminar configuración automática" funcione bien
    fun clearRangos() {
        _rangoMin.value = 0
        _rangoMax.value = 0
    }
}
