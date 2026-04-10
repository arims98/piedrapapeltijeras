package com.example.primeropiedra.View

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.primeropiedra.R
import com.example.primeropiedra.ViewModel.JuegoViewModel
import kotlin.getValue
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.primeropiedra.Model.DBHelper
import com.example.primeropiedra.Services.MusicaService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class MenuInicial : AppCompatActivity() {

    private val viewModel: JuegoViewModel by viewModels()

    var nombreJugador: String = "" // Aqui guardamos el nombre del jugador
    lateinit var btnJugar: Button
    lateinit var btnHistorial: Button
    lateinit var btnConfiguracion: Button
    private lateinit var dbHelper: DBHelper
    private lateinit var rvMini: RecyclerView
    lateinit var historialAdapter: HistorialAdapter
    private lateinit var rvResultados: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        dbHelper = DBHelper(this)

        // Capturamos el nombre y lo guardamos en nuestra variable global
        val nombreJugador = intent.getStringExtra("NOMBRE_JUGADOR") ?: "Jugador"

        // 2. Configuramos el mini recycler
        rvMini = findViewById(R.id.rvMiniHistorial)
        rvMini.layoutManager = LinearLayoutManager(this)

        dbHelper.obtenerHistorialTOP() // <--- Esta es la que SÍ tienes en el DBHelper
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ listaTop ->
                // Usamos el adapter pasándole 'true' para que no salgan las monedas
                historialAdapter = HistorialAdapter(listaTop, true)
                rvMini.adapter = historialAdapter
            }, { error ->
                Log.e("ErrorDB", "No cargó el Top: ${error.message}")
            })

        viewModel.iniBaseDatos(this) // Para conectar la base de datos con la vista

        rvResultados = findViewById(R.id.rvResultadosBusqueda)
        rvResultados.layoutManager = LinearLayoutManager(this)

        // Cargamos TODAS las partidas para que el buscador tenga de dónde sacar
        dbHelper.obtenerHistorialAsync() // Asumiendo que tienes este método
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ listaCompleta ->
                historialAdapter = HistorialAdapter(listaCompleta, false) // false para ver monedas
                rvResultados.adapter = historialAdapter
            }, { /* error */ })

        btnJugar = findViewById(R.id.btnJugar)
        btnHistorial = findViewById(R.id.btnHistorial)
        btnConfiguracion = findViewById(R.id.btnConfiguracion)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        mostrarSaludoBienvenida(nombreJugador)

        btnJugar.setOnClickListener {
            val ticket = Intent(this, JuegoView::class.java)
            ticket.putExtra("nombreUsuario", nombreJugador)
            startActivity(ticket)
        }
        btnHistorial.setOnClickListener {
            val intent = Intent(this, HistorialView::class.java)
            startActivity(intent)
        }
        btnConfiguracion.setOnClickListener {
            val intent = Intent(this, Configuracion::class.java)
            startActivity(intent)
        }

    }
    //Para quitar ajutes del toolbar en esta pagina
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        // 1. Buscamos los items por su ID
        val itemCasita = menu?.findItem(R.id.casita)
        val itemConfig = menu?.findItem(R.id.item_configuracion)

        // 2. Los hacemos invisibles
        itemCasita?.isVisible = false
        itemConfig?.isVisible = false

        return super.onPrepareOptionsMenu(menu)
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

            R.id.item_cerrar_sesion -> {
                val intent = Intent(this, Login::class.java)

                // 2. Limpiamos el historial de pantallas para que no pueda volver al juego
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)

                // 3. Cerramos esta pantalla
                finish()
                true
            }
            R.id.action_ayuda_inicio -> {
                val intent = Intent(this, Ayuda::class.java)
                startActivity(intent)
                return true
                }
            else -> super.onOptionsItemSelected(item) //Aqui debo poner para ir a la pagina de login
        }
    }
    private fun mostrarSaludoBienvenida(nombre: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("¡Hola!")
        builder.setMessage("Bienvenid@, $nombre. Prepárate para el desafío.")

        val dialog = builder.create()
        dialog.show()

        // PROGRAMAMOS EL CIERRE AUTOMÁTICO (a los 2.5 segundos)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 4000)
    }
    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences("AjustesApp", MODE_PRIVATE)
        val musicaActivada = prefs.getBoolean("musica_viva", true)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Preguntamos si hay otras apps sonando ahora mismo
        val otraAppSonando = audioManager.isMusicActive

        if (musicaActivada && !otraAppSonando) {
            // Solo si el usuario quiere música Y Spotify está callado, reanudamos
            val intent = Intent(this, MusicaService::class.java)
            intent.action = "REANUDAR_AUDIO"
            startService(intent)
        }
    }
}
