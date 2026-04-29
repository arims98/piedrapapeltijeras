package com.example.primeropiedra.View

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.primeropiedra.R
import android.content.Intent
import com.example.primeropiedra.Services.MusicaService
import com.example.primeropiedra.Utils.Idioma

class IdiomasActivity : AppCompatActivity(){

    lateinit var btnCatalan: Button
    lateinit var btnCastellano: Button
    lateinit var btnIngles: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.idioma)

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
    override fun onResume() {
        super.onResume()
        // Solo enviamos la orden de reanudar.
        val intent = Intent(this, MusicaService::class.java)
        intent.action = "REANUDAR_AUDIO"
        startService(intent)
    }

    override fun onPause() {
        super.onPause()
        val intent = Intent(this, MusicaService::class.java)
        intent.action = "PAUSAR_AUDIO"
        startService(intent)
    }
}