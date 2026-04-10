package com.example.primeropiedra.View

import android.content.Intent
import android.graphics.Color
import android.media.SoundPool
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import android.os.Handler
import android.media.AudioAttributes
import android.content.Context
import android.media.AudioManager


class Configuracion : AppCompatActivity() {
    lateinit var historialAdapter: HistorialAdapter
    private lateinit var rvResultados: RecyclerView
    var musicaEncendida = true
    lateinit var btnMusica: ImageButton
    private lateinit var soundPool: SoundPool //Para reproducir sonidos rápidos y que no sea lento
    private var sonidoClicId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.configuracion)

        val prefs = getSharedPreferences("AjustesApp", MODE_PRIVATE)
        musicaEncendida = prefs.getBoolean("musica_viva", true)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 1. Cargamos las animaciones
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Permite sonar 5 sonidos a la vez sin cortarse
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                Log.d("SoundPool", "Sonido cargado con éxito, ID: $sampleId")
            } else {
                Log.e("SoundPool", "Error al cargar el sonido")
            }
        }

// Cargamos el sonido
        sonidoClicId = soundPool.load(this, R.raw.clickmusica, 1)

        btnMusica = findViewById(R.id.btnMusica)

        if (musicaEncendida) {
            btnMusica.setImageResource(R.drawable.music)
        } else {
            btnMusica.setImageResource(R.drawable.nomusic)
        }

        btnMusica.setOnClickListener {
            // 1. Preparamos las preferencias
            val prefs = getSharedPreferences("AjustesApp", MODE_PRIVATE)
            val editor = prefs.edit()

            // 2. Lanzamos la animación de desaparecer
            btnMusica.startAnimation(fadeOut)

            // 3. Esperamos a que la imagen sea invisible para cambiarla
            Handler(Looper.getMainLooper()).postDelayed({
                if (musicaEncendida) {
                    // --- MÚSICA SE APAGA ---
                    btnMusica.setImageResource(R.drawable.nomusic)
                    stopService(Intent(this@Configuracion, MusicaService::class.java))

                    musicaEncendida = false
                    editor.putBoolean("musica_viva", false) // Guardamos que está APAGADA
                } else {
                    // --- MÚSICA SE ENCIENDE ---
                    btnMusica.setImageResource(R.drawable.music)
                    startService(Intent(this@Configuracion, MusicaService::class.java))

                    musicaEncendida = true
                    editor.putBoolean("musica_viva", true) // Guardamos que está ENCENDIDA
                }

                //Confirmamos el guardado en la memoria del móvil
                editor.apply()

                // 5. Animación de aparecer y sonido
                btnMusica.startAnimation(fadeIn)
                soundPool.play(sonidoClicId, 1f, 1f, 1, 0, 1f)

            }, 300)
        }
    }
    //Para quitar ajutes del toolbar en esta pagina
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        // 1. Buscamos los items por su ID
        val itemConfig = menu?.findItem(R.id.item_configuracion)
        // 2. Los hacemos invisibles
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
            R.id.casita -> {
                finish()
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
            R.id.action_ayuda_inicio -> {
                val intent = Intent(this, Ayuda::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item) //Aqui debo poner para ir a la pagina de login
        }
    }
    //Para que el sonido del boton de la musica, no se vaya guardando en la RAM
    override fun onDestroy() {
        super.onDestroy()
        if (::soundPool.isInitialized) {
            soundPool.release() // Esto libera la memoria RAM (Punto 1.c)
        }
    }
    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences("AjustesApp", MODE_PRIVATE)
        val musicaActivada = prefs.getBoolean("musica_viva", true)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Preguntamos: ¿Hay música de otra app (Spotify/YouTube) sonando ahora mismo?
        val otraAppSonando = audioManager.isMusicActive

        if (musicaActivada && !otraAppSonando) {
            // Solo si el usuario quiere música Y Spotify está callado, reanudamos
            val intent = Intent(this, MusicaService::class.java)
            intent.action = "REANUDAR_AUDIO"
            startService(intent)
        }
    }
}

