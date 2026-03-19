package com.example.primeropiedra.View

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.RippleConfiguration
import com.example.primeropiedra.R
import com.example.primeropiedra.ViewModel.JuegoViewModel
import kotlinx.coroutines.channels.ticker
import kotlin.getValue

class Menu : AppCompatActivity() {

    private val viewModel: JuegoViewModel by viewModels()
    var nombreJugador: String = "" // Aqui guardamos el nombre del jugador
    lateinit var btnJugar: Button
    lateinit var btnHistorial: Button
    lateinit var btnConfiguracion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        // Capturamos el nombre y lo guardamos en nuestra variable global
        val nombreJugador = intent.getStringExtra("NOMBRE_JUGADOR") ?: "Jugador"

        viewModel.iniBaseDatos(this) // Para conectar la base de datos con la vista

        btnJugar = findViewById(R.id.btnJugar)
        btnHistorial = findViewById(R.id.btnHistorial)
        btnConfiguracion = findViewById(R.id.btnConfiguracion)

        Toast.makeText(this, "Bienvenido $nombreJugador", Toast.LENGTH_LONG).show()

        btnJugar.setOnClickListener {
            val ticket = Intent(this, JuegoView::class.java) // Para ir a la pantalla siguiente del juego
            ticket.putExtra("NOMBRE_JUGADOR", nombreJugador) // Para tambien llevar a la siguiente pantalla,datos extras

            startActivity(ticket)

            // Le pasamos el nombre al JuegoViewModel
            viewModel.setNombreJugador(nombreJugador)


        }
        btnHistorial.setOnClickListener {}
        btnConfiguracion.setOnClickListener {}

    }
}