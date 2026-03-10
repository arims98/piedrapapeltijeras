//Estamos en la carpeta View, esta clase Juego, solo se encarga de los findViewById, de los Toast y de cambiar las imagenes. No toma decisiones.

package com.example.primeropiedra.View

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.primeropiedra.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Juego : AppCompatActivity() {
    var monedas = 10
    var victoriasJugador = 0
    var victoriasPC = 0
    var nombreJugador: String = "" // Aqui guardamos el nombre del jugador

    var tiempoInicio: Long = 0 // Para guardar el momento exacto, en el que inicia el juego al darle al start

    lateinit var btnPiedra: ImageView
    lateinit var btnPapel: ImageView
    lateinit var btnTijeras: ImageView
    lateinit var btnStart: Button
    lateinit var sacoMonedas: TextView
    lateinit var marcador: TextView
    lateinit var manoPC: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego)
        // Capturamos el nombre y lo guardamos en nuestra variable global
        nombreJugador = intent.getStringExtra("NOMBRE_JUGADOR") ?: "Jugador"
        //El marcador
        marcador = findViewById(R.id.marcador)
        val nombre = intent.getStringExtra("NOMBRE_JUGADOR")

        manoPC = findViewById(R.id.manoPC)
        btnStart = findViewById(R.id.btnStart)
        btnPiedra = findViewById(R.id.piedraJugador)
        btnPapel = findViewById(R.id.papelJugador)
        btnTijeras = findViewById(R.id.tijerasJugador)
        sacoMonedas = findViewById(R.id.sacoMonedas)

        // Para que el toolbar funcione
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        desactivamosManos()

        btnStart.setOnClickListener {
            empezamosPartida()
            btnPiedra.isClickable = true
            btnPapel.isClickable = true
            btnTijeras.isClickable = true
            btnStart.isEnabled = false

            tiempoInicio = System.currentTimeMillis() // Captura la hora actual en milisegundos

            Toast.makeText(this, "¡Partida iniciada!", Toast.LENGTH_SHORT).show()
        }

        btnPiedra.setOnClickListener { jugar(1) }
        btnPapel.setOnClickListener { jugar(2) }
        btnTijeras.setOnClickListener { jugar(3) }

        Toast.makeText(this, "Bienvenido $nombre. Tienes $monedas monedas", Toast.LENGTH_LONG).show()
    }

    fun jugar(eleccionJugador: Int) {
        // Hacemos un bloqueo total e inmediato
        btnPiedra.isClickable = false
        btnPapel.isClickable = false
        btnTijeras.isClickable = false
        //Bajamos la opacidad para que se vean que estas desactivados
        btnPapel.alpha = 1.0f
        btnPiedra.alpha = 1.0f
        btnTijeras.alpha = 1.0f

        val opciones = mutableListOf(1, 2, 3)
        opciones.remove(eleccionJugador)
        val eleccionPC = opciones.random()

        // Hacemos visible la imagen
        manoPC.visibility = View.VISIBLE
        actualizarMarcador()

        when (eleccionPC) {
            1 -> manoPC.setImageResource(R.drawable.descargapiedra)
            2 -> manoPC.setImageResource(R.drawable.descargapapel)
            3 -> manoPC.setImageResource(R.drawable.descargatijeras)
        }

        if (eleccionJugador == eleccionPC) {
            Toast.makeText(this, "¡Empate", Toast.LENGTH_SHORT).show()
        } else if ((eleccionJugador == 1 && eleccionPC == 3) ||
            (eleccionJugador == 2 && eleccionPC == 1) ||
            (eleccionJugador == 3 && eleccionPC == 2)) {
            victoriasJugador++
            Toast.makeText(this, "¡Punto para $nombreJugador!", Toast.LENGTH_SHORT).show()
        } else {
            victoriasPC++
            Toast.makeText(this, "¡Punto para la máquina!", Toast.LENGTH_SHORT).show()
        }
        // Esperamos 3 segundos y escondemos la mano del PC para la siguiente ronda
        Handler(Looper.getMainLooper()).postDelayed({
            manoPC.visibility = View.INVISIBLE
            actualizarMarcador()

            //Desbloquemos botones solo si la partida sigue
            if (victoriasJugador < 5 && victoriasPC < 5) {
                btnPiedra.isClickable = true
                btnPapel.isClickable = true
                btnTijeras.isClickable = true
            }
        }, 3000) //Son 3segundos

    }

    fun finalizarPartida() {
        val mensajeFinal: String
        val tiempoFin = System.currentTimeMillis()
        val totalSegundos = (tiempoFin - tiempoInicio) / 1000 // Convertimos a segundos
        val duracionPartida = "$totalSegundos segundos"
        val fechaHoy = obtenerFechaActual()

        if (victoriasJugador > victoriasPC) {
            monedas += 5
            mensajeFinal = "¡ERES EL GANADOR DE LA PARTIDA! +5 monedas"
        } else if (victoriasPC > victoriasJugador) {
            monedas -= 2
            mensajeFinal = "OOOOOH HAS PERDIDO LA PARTIDA... -2 monedas"
        } else {
            mensajeFinal = "¡EMPATE DE LA PARTIDA!... Vuelve a intentarlo...."
        }
        actualizarSaco()
        desactivamosManos()

        // Hacemos aparecer el boton start otra vez
        btnStart.visibility = View.VISIBLE
        btnStart.isEnabled = true
        btnStart.text = "REINTENTAR"
        btnPiedra.isClickable = false
        btnPapel.isClickable = false
        btnTijeras.isClickable = false
        btnPapel.alpha = 0.5f
        btnPiedra.alpha = 0.5f
        btnTijeras.alpha = 0.5f

        Toast.makeText(this, mensajeFinal, Toast.LENGTH_SHORT).show()
    }

    fun reiniciarMarcador() {
        victoriasPC = 0
        victoriasJugador = 0

        //Actualizamos el tecto del marcador inmediatamente
        marcador.text = "PC 0 - $nombreJugador 0"

        //Volvemos a activar los botones
        btnPiedra.isClickable = true
        btnPapel.isClickable = true
        btnTijeras.isClickable = true
        btnPiedra.alpha = 1.0f
        btnPapel.alpha = 1.0f
        btnTijeras.alpha = 1.0f

        Toast.makeText(this, "¡Partida Reiniciada!", Toast.LENGTH_SHORT).show()

    }
    fun actualizarSaco() {
        sacoMonedas.text = monedas.toString()
    }
    fun actualizarMarcador() {
        marcador.text = " PC $victoriasPC - $nombreJugador $victoriasJugador"
        // Actualizamos las monedas
        sacoMonedas.text = monedas.toString()

        //Comprobamos si la partida debe terminar
        if (victoriasJugador == 5 || victoriasPC == 5) {
            finalizarPartida()
        } else if (victoriasPC == 5) {
            finalizarPartida()
        }
    }
    fun desactivamosManos() {
        btnPiedra.isClickable = false
        btnPapel.isClickable = false
        btnTijeras.isClickable = false
        //Bajamos la opacidad para que se vean que estas desactivados
        btnPapel.alpha = 0.5f
        btnPiedra.alpha = 0.5f
        btnTijeras.alpha = 0.5f
    }
    fun empezamosPartida() {
        victoriasPC = 0
        victoriasJugador = 0
        marcador.text = "PC 0 - $nombreJugador 0"
        // Hacemos desaparecer el botón Start
        btnStart.visibility = View.GONE

        // Activamos las manos con intensidad total
        activamosManos()
    }

    fun activamosManos() {
        btnPiedra.isClickable = true
        btnPapel.isClickable = true
        btnTijeras.isClickable = true
        // Subimos la opacidad
        btnPapel.alpha = 1.0f
        btnPiedra.alpha = 1.0f
        btnTijeras.alpha = 1.0f
    }
    fun obtenerFechaActual(): String {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fecha = Date() //Obtiene la fecha y hora actual del sistema
        return formato.format(fecha)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navegacion, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_reinicio -> {
                // Aquí podrías resetear incluso las monedas si quieres
                monedas = 10
                actualizarSaco()


                // Reseteamos el marcador y habilitar el boton Start
                reiniciarMarcador()
                btnStart.visibility = View.VISIBLE
                btnStart.isEnabled = true
                btnStart.text = "¡START!"

                desactivamosManos()

                Toast.makeText(this, "JUEGO RESETEADO POR COMPLETO", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.item_salir -> {
                // Cerramos esta actividad y volvemos a la anterior (Login)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item) //Aqui debo poner para ir a la pagina de login
        }
    }
}