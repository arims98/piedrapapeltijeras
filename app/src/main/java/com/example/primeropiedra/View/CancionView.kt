package com.example.primeropiedra.View

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.primeropiedra.R
import com.example.primeropiedra.Services.MusicaService


class CancionView : AppCompatActivity() {
    lateinit var btnElegir: Button
    lateinit var btnTuCancion: ImageButton
    var musicaEncendida = true
    private lateinit var soundPool: SoundPool //Para reproducir sonidos rápidos y que no sea lento
    private var sonidoClicId: Int = 0
    lateinit var historialAdapter: HistorialAdapter
    private lateinit var rvResultados: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cancion)

        val toolbar: androidx.appcompat.widget.Toolbar? = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val prefs = getSharedPreferences("AjustesApp", MODE_PRIVATE)
        musicaEncendida = prefs.getBoolean("musica_viva", true)


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

        btnElegir = findViewById(R.id.btnElegir)

        btnElegir.setOnClickListener {
            // Al pulsar el botón "Escoge tu canción"
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*" // Filtramos para que solo salgan archivos de audio
            }
            startActivityForResult(intent, 2002)
        }
        btnTuCancion = findViewById(R.id.btnTuCancion)

        // Comprobamos el estado inicial nada más abrir la pantalla
        if (musicaEncendida) {
            btnTuCancion.setImageResource(R.drawable.music)
        } else {
            btnTuCancion.setImageResource(R.drawable.nomusic)
        }

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        btnTuCancion.setOnClickListener {
            val editor = prefs.edit()
            btnTuCancion.startAnimation(fadeOut)

            Handler(Looper.getMainLooper()).postDelayed({
                val intentService = Intent(this, MusicaService::class.java)

                if (musicaEncendida) {
                    // --- MÚSICA SE APAGA ---
                    btnTuCancion.setImageResource(R.drawable.nomusic)

                    intentService.action = "PAUSAR_AUDIO"
                    startService(intentService)

                    musicaEncendida = false
                    editor.putBoolean("musica_viva", false)
                } else {
                    // --- MÚSICA SE ENCIENDE ---
                    btnTuCancion.setImageResource(R.drawable.music)

                    intentService.action = "REANUDAR_AUDIO"
                    startService(intentService)

                    musicaEncendida = true
                    editor.putBoolean("musica_viva", true)
                }

                editor.apply()
                btnTuCancion.startAnimation(fadeIn)
                soundPool.play(sonidoClicId, 1f, 1f, 1, 0, 1f)
            }, 300)

        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2002 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)

                val prefs = getSharedPreferences("AjustesApp", Context.MODE_PRIVATE)
                prefs.edit().putString("uri_personalizada", uri.toString()).apply()

                //Toast.makeText(this, "Canción seleccionada con éxito", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, getString(R.string.text_cancionseleccionada), Toast.LENGTH_SHORT).show()

                // Avisamos al servicio para que cambie la música YA ---
                val intentMusica = Intent(this, com.example.primeropiedra.Services.MusicaService::class.java)
                intentMusica.action = "REINICIAR_AUDIO"
                startService(intentMusica)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navegacion, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as androidx.appcompat.widget.SearchView

        searchView.queryHint = getString(R.string.hint_buscar_partida)

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
                        // 3. Si el usuario escribe, mostramos el listado de resultados encima
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

            R.id.item_configuracion -> {
                val intent = Intent(this, Configuracion::class.java)
                startActivity(intent)
                return true
            }

            R.id.action_ayuda_inicio -> {
                val intent = Intent(this, Ayuda::class.java)
                startActivity(intent)
                return true

            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}