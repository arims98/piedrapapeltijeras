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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.primeropiedra.R
import com.example.primeropiedra.ViewModel.JuegoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JuegoView : AppCompatActivity() {

    // Importante: Asegúrate de tener la dependencia activity-ktx en el build.gradle
    private val viewModel: JuegoViewModel by viewModels()
    var nombreJugador: String = "" // Aqui guardamos el nombre del jugador
    lateinit var btnPiedra: ImageView
    lateinit var btnPapel: ImageView
    lateinit var btnTijeras: ImageView
    lateinit var btnStart: Button
    lateinit var sacoMonedas: TextView
    lateinit var marcador: TextView
    lateinit var manoPC: ImageView
    lateinit var Mensajes: TextView
    private var segundosTranscurridos = 0
    private var runnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego)

        // Le pasamos el nombre al JuegoViewModel
        val nombreRecuperado = intent.getStringExtra("nombreUsuario") ?: "Invitado"
        viewModel.setNombreJugador(nombreRecuperado, this)

        //El marcador
        marcador = findViewById(R.id.marcador)

        val nombre = intent.getStringExtra("NOMBRE_JUGADOR")

        viewModel.iniBaseDatos(this) // Para conectar la base de datos con la vista

        manoPC = findViewById(R.id.manoPC)
        btnStart = findViewById(R.id.btnStart)
        btnPiedra = findViewById(R.id.piedraJugador)
        btnPapel = findViewById(R.id.papelJugador)
        btnTijeras = findViewById(R.id.tijerasJugador)
        sacoMonedas = findViewById(R.id.sacoMonedas)
        Mensajes = findViewById(R.id.Mensajes)

        // Para que el toolbar funcione
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        desactivamosManos()

        observamosDatos()

        btnStart.setOnClickListener {
            activamosManos()
            viewModel.iniciarPartida() //El JuegoViewModel, resetea los números
            viewModel.iniciarCronometro()
            iniciarVisualCrono()
           btnStart.visibility = View.GONE // Lo ocultamos al empezar

            Toast.makeText(this, "¡Partida iniciada!", Toast.LENGTH_SHORT).show()
        }

        btnPiedra.setOnClickListener { jugar(1) }
        btnPapel.setOnClickListener { jugar(2) }
        btnTijeras.setOnClickListener { jugar(3) }

        viewModel.mensajeEstado.observe(this) { mensaje ->
            Mensajes.text = mensaje
            Mensajes.visibility = View.INVISIBLE

            // Ahora para que desaparezca el mensaje 2 segundo despues
            Handler(Looper.getMainLooper()).postDelayed({
                Mensajes.visibility = View.INVISIBLE
            }, 2000)
        }
    }
    fun jugar(eleccionJugador: Int) {
        desactivamosManos()
        viewModel.jugar(eleccionJugador) //El motor hace el random y suma puntos

        val vJugador = viewModel.victoriasJugador.value ?: 0
        val vIA = viewModel.victoriasIA.value ?: 0

        if (vJugador >= 5 || vIA >= 5) {
            finalizarPartida()
        }

        // Esperamos 3 segundos y escondemos la mano del PC para la siguiente ronda
        Handler(Looper.getMainLooper()).postDelayed({
            manoPC.visibility = View.INVISIBLE

            if (vJugador < 5 && vIA < 5) {
                activamosManos()
            }
        }, 2000) //Son 2 segundos

    }
    fun finalizarPartida() {
        detenerCronoVisual()

        viewModel.calcularResultadoFinal()

        val segundos = viewModel.obtenerDuracionSegundos()
        val fechaHoy = obtenerFechaActual()

        // Ahora a la base de datos
        viewModel.registrarPartidaBD(segundos, fechaHoy)

        // Ahora lo hacemos visual
        desactivamosManos()
        btnStart.visibility = View.VISIBLE
        btnStart.isEnabled = true
        btnStart.text = "REINTENTAR"
    }
    fun desactivamosManos() {

        btnPiedra.isEnabled = false
        btnPapel.isEnabled = false
        btnTijeras.isEnabled = false

        //Bajamos la opacidad para que se vean que estas desactivados
        btnPapel.alpha = 0.5f
        btnPiedra.alpha = 0.5f
        btnTijeras.alpha = 0.5f
    }

    fun activamosManos() {

        btnPiedra.isEnabled = true
        btnPapel.isEnabled = true
        btnTijeras.isEnabled = true

        // Subimos la opacidad
        btnPapel.alpha = 1.0f
        btnPiedra.alpha = 1.0f
        btnTijeras.alpha = 1.0f
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navegacion, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_reinicio -> {
                // Aquí podrías resetear incluso las monedas si quieres
                viewModel.iniciarPartida() //Usamos el reset del motor
                btnStart.visibility = View.VISIBLE
                btnStart.text = "¡START"
                desactivamosManos()
                Toast.makeText(this, "JUEGO RESETEADO", Toast.LENGTH_SHORT).show()
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
    private fun observamosDatos() {
        // Cuando cambie el marcador en el JuegoViewModel, actualizamos el TextView del marcador
        viewModel.Marcador.observe(this) { nuevoMarcador ->
            marcador.text = nuevoMarcador
        }
        // Cuando cambien las monedas en el JuegoViewModel, actualizamos el saco
        viewModel.monedas.observe(this) { cantidad ->
            sacoMonedas.text = cantidad.toString()
        }
        viewModel.eleccionIA.observe(this) { eleccionIA ->
            manoPC.visibility = View.VISIBLE
            when (eleccionIA) {
                1 -> manoPC.setImageResource(R.drawable.descargapiedra)
                2 -> manoPC.setImageResource(R.drawable.descargapapel)
                3 -> manoPC.setImageResource(R.drawable.descargatijeras)
            }
        }
    }
    fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
    fun iniciarVisualCrono() {
        // Ponemos la fecha actual al empezar
        findViewById<TextView>(R.id.tvFechaSesion).text = "Fecha: ${obtenerFechaActual()}"

        segundosTranscurridos = 0
        runnable = object : Runnable {
            override fun run() {
                segundosTranscurridos++
                val minutos = segundosTranscurridos / 60
                val segundos = segundosTranscurridos % 60
                // Formateamos para que siempre tenga dos dígitos (00:00)
                findViewById<TextView>(R.id.tvCronometro).text =
                    String.format("Tiempo: %02d:%02d", minutos, segundos)

                handler.postDelayed(this, 1000) // Se ejecuta cada segundo
            }
        }
        handler.postDelayed(runnable!!, 1000)
    }
    fun detenerCronoVisual() {
        runnable?.let { handler.removeCallbacks(it) }
    }
}
