package com.example.primeropiedra.View

import android.content.Intent
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
import android.app.AlertDialog // Para los carteles de victoria/derrota profesionales
import android.graphics.Color
import android.util.Log
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.example.primeropiedra.Model.DBHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import androidx.recyclerview.widget.LinearLayoutManager

class JuegoView : AppCompatActivity() {
    private val viewModel: JuegoViewModel by viewModels()
    var nombreJugador: String = ""
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
    lateinit var historialAdapter: HistorialAdapter
    private lateinit var rvResultados: RecyclerView
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego)

        // Recuperar nombre y configurar ViewModel
        val nombreRecuperado = intent.getStringExtra("nombreUsuario") ?: "Invitado"
        viewModel.setNombreJugador(nombreRecuperado, this)
        viewModel.iniBaseDatos(this)

        // Vincular vistas
        marcador = findViewById(R.id.marcador)
        manoPC = findViewById(R.id.manoPC)
        btnStart = findViewById(R.id.btnStart)
        btnPiedra = findViewById(R.id.piedraJugador)
        btnPapel = findViewById(R.id.papelJugador)
        btnTijeras = findViewById(R.id.tijerasJugador)
        sacoMonedas = findViewById(R.id.sacoMonedas)
        Mensajes = findViewById(R.id.Mensajes)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        desactivamosManos()

        observamosDatos()

        mostrarmensajejuego()

        dbHelper = DBHelper(this)
        rvResultados = findViewById(R.id.rvResultadosBusqueda)
        rvResultados.layoutManager = LinearLayoutManager(this)

// Cargar los datos para que el buscador tenga qué filtrar
        dbHelper.obtenerHistorialAsync()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ listaCompleta ->
                // Inicializamos el adapter con todos los datos de la BD
                historialAdapter = HistorialAdapter(listaCompleta, false)
                rvResultados.adapter = historialAdapter
                Log.d("Busqueda", "Datos cargados correctamente: ${listaCompleta.size} partidas")
            }, { error ->
                Log.e("ErrorDB", "No se pudieron cargar los datos para el buscador: ${error.message}")
            })

        btnStart.setOnClickListener {
            activamosManos()
            viewModel.iniciarPartida()
            viewModel.iniciarCronometro()
            iniciarVisualCrono()
            btnStart.visibility = View.GONE
            Toast.makeText(this, "¡Partida iniciada!", Toast.LENGTH_SHORT).show()
        }

        btnPiedra.setOnClickListener { jugar(1) }
        btnPapel.setOnClickListener { jugar(2) }
        btnTijeras.setOnClickListener { jugar(3) }

        viewModel.mensajeEstado.observe(this) { mensaje ->
            Mensajes.text = mensaje
            Mensajes.visibility = View.VISIBLE // Corregido a VISIBLE para que se vea el mensaje
            Handler(Looper.getMainLooper()).postDelayed({
                Mensajes.visibility = View.INVISIBLE
            }, 2000)
        }
    }

    fun jugar(eleccionJugador: Int) {
        desactivamosManos()
        viewModel.jugar(eleccionJugador)

        val vJugador = viewModel.victoriasJugador.value ?: 0
        val vIA = viewModel.victoriasIA.value ?: 0

        if (vJugador >= 5 || vIA >= 5) {
            finalizarPartida()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            manoPC.visibility = View.INVISIBLE
            if (vJugador < 5 && vIA < 5) {
                activamosManos()
            }
        }, 2000)
    }

    fun finalizarPartida() {
        detenerCronoVisual()
        viewModel.calcularResultadoFinal()

        val segundos = viewModel.obtenerDuracionSegundos()
        val fechaHoy = obtenerFechaActual()
        viewModel.registrarPartidaBD(segundos, fechaHoy)

        desactivamosManos()

        // --- LANZAMOS EL CARTEL DE VICTORIA O DERROTA ---
        val ganoJugador = (viewModel.victoriasJugador.value ?: 0) >= 5
        mostrarCartelFinal(
            victoria = ganoJugador,
            puntosJugador = viewModel.victoriasJugador.value ?: 0,
            puntosIA = viewModel.victoriasIA.value ?: 0
        )
    }

    // --- NUEVA FUNCIÓN PARA EL CARTEL PROFESIONAL ---
    private fun mostrarCartelFinal(victoria: Boolean, puntosJugador: Int, puntosIA: Int) {
        val builder = AlertDialog.Builder(this)

        if (victoria) {
            builder.setTitle("🏆 ¡VICTORIA!")
            builder.setMessage("¡Enhorabuena! Has ganado por $puntosJugador a $puntosIA.\n¿Qué quieres hacer ahora?")
            builder.setIcon(android.R.drawable.btn_star_big_on)
        } else {
            builder.setTitle("😢 DERROTA")
            builder.setMessage("Vaya... la IA te ha ganado $puntosIA a $puntosJugador.\n¡Sigue practicando para el TOP 3!")
            builder.setIcon(android.R.drawable.ic_delete)
        }

        builder.setPositiveButton("REINTENTAR") { _, _ ->
            // Simulamos el click del botón start para reiniciar todo
            btnStart.performClick()
        }

        builder.setNegativeButton("VER HISTORIAL") { _, _ ->
            val intent = Intent(this, HistorialView::class.java)
            startActivity(intent)
            finish()
        }

        val dialog = builder.create()
        dialog.setCancelable(false) // Obligamos a elegir una opción
        dialog.show()
    }

    fun desactivamosManos() {
        btnPiedra.isEnabled = false
        btnPapel.isEnabled = false
        btnTijeras.isEnabled = false
        btnPapel.alpha = 0.5f
        btnPiedra.alpha = 0.5f
        btnTijeras.alpha = 0.5f
    }

    fun activamosManos() {
        btnPiedra.isEnabled = true
        btnPapel.isEnabled = true
        btnTijeras.isEnabled = true
        btnPapel.alpha = 1.0f
        btnPiedra.alpha = 1.0f
        btnTijeras.alpha = 1.0f
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navegacion, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = "Buscar partida..."

        // Personalización de colores (para que no falle, usamos un try-catch o safe call)
        val searchText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchText?.setTextColor(Color.BLACK)
        searchText?.setHintTextColor(Color.GRAY)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 1. Verificamos que el adapter esté inicializado para evitar crasheos
                if (::historialAdapter.isInitialized) {

                    if (newText.isNullOrEmpty()) {
                        // 2. Si no hay texto, escondemos el listado de búsqueda y vemos el menú normal
                        rvResultados.visibility = android.view.View.GONE
                    } else {
                        // 3. Si el usuario escribe, mostramos el listado de resultados encima de TODO
                        rvResultados.visibility = android.view.View.VISIBLE

                        // 4. Ejecutamos el filtrado con el texto que escribió el usuario
                        historialAdapter.filtrar(newText)
                    }
                }
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.casita -> {
                finish()
                true
            }
            R.id.action_ayuda_inicio -> {
                mostrarPopUpAyuda()
                true
            }
            R.id.item_cerrar_sesion -> {
                val intent = Intent(this, Login::class.java)

                // 2. Limpiamos el historial de pantallas para que no pueda volver al juego
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)

                // 3. Cerramos esta pantalla
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun mostrarPopUpAyuda() {
        AlertDialog.Builder(this)
            .setTitle("Reglas del Juego")
            .setMessage("• Piedra > Tijera\n• Papel > Piedra\n• Tijera > Papel\n\n¡Gana el primero que llegue a 5 puntos!")
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun observamosDatos() {
        viewModel.Marcador.observe(this) { nuevoMarcador ->
            marcador.text = nuevoMarcador
        }
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
        findViewById<TextView>(R.id.tvFechaSesion).text = "Fecha: ${obtenerFechaActual()}"
        segundosTranscurridos = 0
        runnable = object : Runnable {
            override fun run() {
                segundosTranscurridos++
                val minutos = segundosTranscurridos / 60
                val segundos = segundosTranscurridos % 60
                findViewById<TextView>(R.id.tvCronometro).text =
                    String.format("Tiempo: %02d:%02d", minutos, segundos)
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun detenerCronoVisual() {
        runnable?.let { handler.removeCallbacks(it) }
    }
    private fun mostrarmensajejuego() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("¡Empieza el juego!")
        builder.setMessage("¡Consigue 5 Victorias y gana a la IA!")

        val dialog = builder.create()
        dialog.show()

        // PROGRAMAMOS EL CIERRE AUTOMÁTICO (a los 2.5 segundos)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 4000)
    }
}
