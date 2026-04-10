package com.example.primeropiedra.View

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.primeropiedra.R
import com.example.primeropiedra.Services.MusicaService

class PantallaCarga: AppCompatActivity() {
    var musicaEncendida = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super .onCreate(savedInstanceState)
        //Aqui esta el diseño
        setContentView(R.layout.activity_pantalla_carga)

        val prefs = getSharedPreferences("AjustesApp", MODE_PRIVATE)
        val musicaDeberiaSonar = prefs.getBoolean("musica_viva", true)

        if (musicaDeberiaSonar) {
            startService(Intent(this, MusicaService::class.java))
        }
        //Temporizar de 3 segundos para saltar al login
        Handler(Looper.getMainLooper()).postDelayed({
            //Creamos el mensajero Intent
            //De 'this'(esta pantalla) a 'Login::class.java' (la pantalla que creamos de login)
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            //Cerramos la carga para que no pueda volver atraás con el botón del móvil
            finish()

        }, 3000)
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
