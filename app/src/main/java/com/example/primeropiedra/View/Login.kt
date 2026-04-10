package com.example.primeropiedra.View

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.primeropiedra.R
import com.example.primeropiedra.Services.MusicaService


class Login: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        //1. Declaramos las variables para los componentes
        val jugadorNombre = findViewById<EditText>(R.id.etNombreUsuario)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        //2. Escuchamos cuando el jugador hace clic en el botón
        btnEntrar.setOnClickListener{
            //3.Obtenemos el texto que el usuario ha escrito y lo comvertimos en una cadena de texto
            val nombreEscrito = jugadorNombre.text.toString()
            //4. Comprobamos si no estávacio para que no juegue nadie sin nombre
            if (nombreEscrito.isNotEmpty()) {
                //Pasamos a la pantalla de juego(las siguiente), llevando el nombre
                val intent = Intent(this, MenuInicial::class.java)
                intent.putExtra("NOMBRE_JUGADOR", nombreEscrito)
                startActivity(intent)
                finish()
            } else
                //Si no escribio nada y le ha dado al boton
                jugadorNombre.error="Por favor, escribe tu nombre"

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