package com.example.primeropiedra.View

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.primeropiedra.R

class PantallaCarga: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super .onCreate(savedInstanceState)
        //Aqui esta el diseño
        setContentView(R.layout.activity_pantalla_carga)


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
}