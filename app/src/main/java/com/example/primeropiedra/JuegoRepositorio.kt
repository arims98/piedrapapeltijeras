package com.example.primeropiedra

import com.example.primeropiedra.network.JugadorPuntuacion
import com.example.primeropiedra.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JuegoRepositorio {

    private val api = RetrofitClient.instancia

    /**
     * Sube las victorias del jugador a Firebase en un hilo secundario
     * para que la pantalla del juego no se congele.
     */
    suspend fun guardarPuntuacion(nombreJugador: String, victorias: Int, idUnico: String) {
        withContext(Dispatchers.IO) {
            try {
                val datos = JugadorPuntuacion(
                    nombre = nombreJugador,
                    victorias = victorias,
                    uid = idUnico
                )
                // Hacemos la llamada HTTP PUT usando Retrofit
                api.actualizarPuntuacion(idUnico, datos)
            } catch (e: Exception) {
                println("--- ERROR AL GUARDAR EN FIREBASE: ${e.message} ---")
                e.printStackTrace() // Si no hay internet, evita que la app se cierre
            }
        }
    }
}