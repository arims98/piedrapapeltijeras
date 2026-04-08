package com.example.primeropiedra.View

import android.content.Intent
import android.graphics.Color
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
import android.widget.EditText
import androidx.appcompat.widget.SearchView

class HistorialView : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var recyclerView: RecyclerView
    lateinit var historialAdapter: HistorialAdapter
    private lateinit var rvResultados: RecyclerView



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
}