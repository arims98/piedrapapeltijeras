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
import com.example.primeropiedra.Model.DBHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import android.util.Log

class HistorialView : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial)

        dbHelper = DBHelper(this)

        // 1. Configuramos el RecyclerView
        recyclerView = findViewById(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Para que el toolbar funcione
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        cargarDatos()
    }
    fun cargarDatos() {
        dbHelper.obtenerHistorialAsync()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ lista ->
                // "lista" aquí es de tipo List<PartidaTabla>
                val adapter = HistorialAdapter(lista)
                recyclerView.adapter = adapter
            }, { error ->
                Log.e("ErrorDB", "No se pudo cargar el historial: ${error.message}")
            })
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_navegacion, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //  La flecha de atrás de la propia Toolbar (ID estándar de Android)
            android.R.id.home -> {
                finish() // Simplemente cierra esta pantalla y vuelve a la anterior
                true
            }
            R.id.casita -> {
                // El icono de la casa te devuelve al Menú Principal (Login)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item) //Aqui debo poner para ir a la pagina de login
        }
    }
}