package com.example.primeropiedra.View

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.primeropiedra.R
import com.example.primeropiedra.ViewModel.JuegoViewModel
import kotlin.getValue
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.primeropiedra.Model.DBHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class MenuInicial : AppCompatActivity() {

    private val viewModel: JuegoViewModel by viewModels()

    var nombreJugador: String = "" // Aqui guardamos el nombre del jugador
    lateinit var btnJugar: Button
    lateinit var btnHistorial: Button
    lateinit var btnConfiguracion: Button
    private lateinit var dbHelper: DBHelper
    private lateinit var rvMini: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        dbHelper = DBHelper(this)

        // Capturamos el nombre y lo guardamos en nuestra variable global
        val nombreJugador = intent.getStringExtra("NOMBRE_JUGADOR") ?: "Jugador"

        // 2. Configuramos el mini recycler
                rvMini = findViewById(R.id.rvMiniHistorial)
        rvMini.layoutManager = LinearLayoutManager(this)

        // 3. Llamamos a la función
        cargarMiniHistorial()



        viewModel.iniBaseDatos(this) // Para conectar la base de datos con la vista

        btnJugar = findViewById(R.id.btnJugar)
        btnHistorial = findViewById(R.id.btnHistorial)
        btnConfiguracion = findViewById(R.id.btnConfiguracion)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        Toast.makeText(this, "Bienvenido $nombreJugador", Toast.LENGTH_LONG).show()

        btnJugar.setOnClickListener {
            val ticket = Intent(this, JuegoView::class.java)
            ticket.putExtra("nombreUsuario", nombreJugador)
            startActivity(ticket)

        }
        btnHistorial.setOnClickListener {
            val intent = Intent(this, HistorialView::class.java)
            startActivity(intent)

        }
        btnConfiguracion.setOnClickListener {}




    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navegacion, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_salir -> {
                val intent = android.content.Intent(this, Login::class.java)
                // Las flags limpian las pantallas anteriores para que no pueda volver hacia atras
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            //  La flecha de atrás de la propia Toolbar (ID estándar de Android)
            android.R.id.home -> {
                finish() // Simplemente cierra esta pantalla y vuelve a la anterior
                true
            }
            else -> super.onOptionsItemSelected(item) //Aqui debo poner para ir a la pagina de login
        }
    }
    private fun cargarMiniHistorial() {
        // Traemos solo las últimas 3 partidas
        dbHelper.obtenerHistorialTOP()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ lista ->
                // REUTILIZAMOS el mismo adaptador que ya creamos. ¡No hace falta crear otro!
                val adapter = HistorialAdapter(lista)
                rvMini.adapter = adapter
            }, { error ->
                Log.e("MiniHistorial", "Error: ${error.message}")
            })
    }
}
