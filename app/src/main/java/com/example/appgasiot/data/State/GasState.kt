package com.example.appgasiot.data.state

import com.example.appgasiot.data.model.GasConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object GasState {

    private val _configGas = MutableStateFlow<GasConfig?>(null)
    val configGas = _configGas.asStateFlow()

    fun actualizar(min: Int, max: Int) {
        _configGas.value = GasConfig(min, max)
    }

    fun limpiar() {
        _configGas.value = null
    }
}
