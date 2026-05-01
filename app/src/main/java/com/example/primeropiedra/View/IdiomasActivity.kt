package com.example.primeropiedra.View

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.primeropiedra.R
import android.content.Intent
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toolbar
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.example.primeropiedra.Services.MusicaService
import com.example.primeropiedra.Utils.Idioma

class IdiomasActivity : AppCompatActivity(){

    lateinit var btnCatalan: Button
    lateinit var btnCastellano: Button
    lateinit var btnIngles: Button
    lateinit var historialAdapter: HistorialAdapter
    private lateinit var rvResultados: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.idioma)

        val toolbar: androidx.appcompat.widget.Toolbar? = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        btnCatalan = findViewById(R.id.btnCatalan)
        btnCastellano = findViewById(R.id.btnCastellano)
        btnIngles = findViewById(R.id.btnIngles)

        btnCatalan.setOnClickListener {
            Idioma.cambiar(this, "ca")

            val intent = Intent(this, MenuInicial::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        btnCastellano.setOnClickListener {
            Idioma.cambiar(this, "es")

            val intent = Intent(this, MenuInicial::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        btnIngles.setOnClickListener {
            Idioma.cambiar(this, "en")

            //Para que el cambio se vea, hay que reiniciar la pantalla
            val intent = Intent(this, MenuInicial::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
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