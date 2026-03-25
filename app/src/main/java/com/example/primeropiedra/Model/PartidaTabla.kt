package com.example.primeropiedra.Model

data class PartidaTabla (
    val id: Int,
    val nombre: String,
    val monedas: Int,
    val fecha: String,
    val duracion: Int,
    val resultadoJugador: Int,
    val resultadoIA: Int
)
