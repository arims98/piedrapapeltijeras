package com.example.primeropiedra

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast // También lo necesitaremos para los mensajes
import com.example.primeropiedra.View.Juego


class Login: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        //1. Declaramos las variables para los componentes
        val jugadorNombre = findViewById<EditText>(R.id.etNombreUsuario)
        val botonJugar = findViewById<Button>(R.id.btnJugar)
        //2. Escuchamos cuando el jugador hace clic en el botón
        botonJugar.setOnClickListener{
            //3.Obtenemos el texto que el usuario ha escrito y lo comvertimos en una cadena de texto
            val nombreEscrito = jugadorNombre.text.toString()
            //4. Comprobamos si no estávacio para que no juegue nadie sin nombre
            if (nombreEscrito.isNotEmpty()) {
                //Pasamos a la pantalla de juego(las siguiente), llevando el nombre
                val intent = Intent(this, Juego::class.java)
                intent.putExtra("NOMBRE_JUGADOR", nombreEscrito)
                startActivity(intent)
            } else
                //Si no escribio nada y le ha dado al boton
                jugadorNombre.error="Por favor, escribe tu nombre"

        }


    }
}