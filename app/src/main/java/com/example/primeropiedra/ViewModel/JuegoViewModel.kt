/* ¿Qué es el ViewModel de verdad?
Imagina que el XML es la carrocería del coche (lo que ves) y el Juego.kt es el salpicadero (los botones que tocas).

El ViewModel es el MOTOR. El motor no se ve, está escondido bajo el capó.

Si tú pisas el acelerador (botón en Juego.kt), el motor (ViewModel) hace la explosión y decide a qué velocidad vas.

El salpicadero (Juego.kt) solo te enseña una aguja moviéndose, pero no sabe por qué se mueve. El motor es el que tiene la fuerza y los datos.

En Android, se inventó el ViewModel por un problema de "memoria". Si tú giras el móvil, Android destruye y vuelve a crear Juego.kt. Si tus monedas estaban ahí, ¡pum!, desaparecen.
El ViewModel es una "caja fuerte" que sobrevive a ese giro. Por eso el profesor lo pide: porque es la forma profesional de que no se pierdan los datos.*/

package com.example.primeropiedra.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.Context
import com.example.primeropiedra.Model.DBHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable


// Heredamos de ViewModel para que Android sepa que esta es la "caja fuerte"
class JuegoViewModel : ViewModel() {

    // MutableLiveData es una "pizarra" que cambia.
    // La View (Juego.kt) estará mirando estas pizarras.
    val monedas = MutableLiveData<Int>(10)
    val victoriasJugador = MutableLiveData<Int>(0)
    val victoriasIA = MutableLiveData<Int>(0)
    val Marcador = MutableLiveData<String>("Juego 0 - Jugador 0")
    val eleccionIA = MutableLiveData<Int>()
    val mensajeEstado = MutableLiveData<String>() // Como una pizarra para el mensaje del estado de cada jugada
    private var nombreUsuario: String = "" // Es para quitar los toast y poner texto de verdad
    private var tiempoInicio: Long = 0
    private lateinit var db: DBHelper // La inicializamos mas tarde
    private val disposables = CompositeDisposable() // Para cuando utilizad RxJava y el usuario cierra la app, no se pierda nada

    // Función para recibir el nombre del Login
    fun setNombreJugador(nombre: String) {
        nombreUsuario = nombre
        actualizarMarcador()
    }

    // El motor decide qué número saca la máquina
    fun jugar(eleccionJugador: Int) {
        val random = (1..3).random()
        eleccionIA.value = random // Escribe en la pizarra, la View lo verá

        // Lógica de quién gana (el motor calcula)
        if (eleccionJugador == random) {
            mensajeEstado.value = "¡Empate en esta ronda!"
        } else if ((eleccionJugador == 1 && random == 3) ||
            (eleccionJugador == 2 && random == 1) ||
            (eleccionJugador == 3 && random == 2)
        ) {
            victoriasJugador.value = (victoriasJugador.value ?: 0) + 1
            mensajeEstado.value = "¡Punto para $nombreUsuario!"
        } else {
            victoriasIA.value = (victoriasIA.value ?: 0) + 1
            mensajeEstado.value = "La máquina gana..."
        }
        actualizarMarcador()
    }
    private fun actualizarMarcador() {
        Marcador.value = "PC ${victoriasIA.value} - $nombreUsuario ${victoriasJugador.value}"
    }
    fun iniciarPartida() {
        victoriasJugador.value = 0
        victoriasIA.value = 0
        actualizarMarcador()
    }
    fun calcularResultadoFinal() {
        val vJ = victoriasJugador.value ?: 0
        val vIA = victoriasIA.value ?: 0

        if ( vJ >= 5) {
            monedas.value = (monedas.value ?: 0) +5
            mensajeEstado.value = "¡VICTORIA TOTAL!!! +5 MONEDAS"
        } else if (vIA >= 5) {
            monedas.value = (monedas.value ?: 0) -2
            mensajeEstado.value = "OOOOOOH... DERROTAAA... -2 MONEDAS"
        }
    }
    fun iniciarCronometro() {
        // Guardamos el momento exacto en milisegundos, en el que empieza
        tiempoInicio = System.currentTimeMillis()
    }
    fun obtenerDuracionSegundos(): Int {
        val tiempoFin = System.currentTimeMillis()
        // Calculamos la diferencia y pasamos de milisegundos a segundos
        return ((tiempoFin - tiempoInicio) / 1000).toInt()
    }
    fun iniBaseDatos(context: Context) {
        db = DBHelper(context)
    }
    //Para registrar la partida en la base de datos
    fun registrarPartidaBD(duracion: Int, fecha: String) {
        val nombre = nombreUsuario
        val totalMonedas = monedas.value ?: 0

        // Chibato, para ver que falla
        disposables.add(
            db.guardarPartidaAsync(nombre, totalMonedas, fecha, duracion)
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe({
                    println("DEBUG: ¡Guardado con éxito!") // CHIVATO 2
                    mensajeEstado.value = "Partida guardada"
                }, { error ->
                    println("DEBUG: Error al guardar: ${error.message}") // CHIVATO 3
                })
        )

        /* Guardamos la tarea en la bolsa, usando add
        disposables.add(
            db.guardarPartidaAsync(nombre, totalMonedas, fecha, duracion)
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe({
                    mensajeEstado.value = "Partida guardada en el historial"
                }, { error ->
                    mensajeEstado.value = "Error al guardar: ${error.message}"
                })
        )*/
    }
    // Limpiamos cuando el viewmodel se destruya, para no gastar tanta RAM o bateria cuando el usuario sale del juego
    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}