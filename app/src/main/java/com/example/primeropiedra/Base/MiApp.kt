package com.example.primeropiedra

import android.app.Application
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.primeropiedra.Services.MusicaService
import com.example.primeropiedra.Utils.Idioma

class MiApp : Application(), DefaultLifecycleObserver {

    override fun onCreate() {
        super<Application>.onCreate()
        // Registramos el observador que vigila toda la aplicación
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        val prefs = getSharedPreferences("AjustesApp", MODE_PRIVATE)
        val lang = prefs.getString("idioma_seleccionado", "es") ?: "es"
        Idioma.cambiar(this, lang)
    }

    // Se ejecuta cuando la app pasa a segundo plano (Home o cambio de app)
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        val intent = Intent(this, MusicaService::class.java)
        intent.action = "PAUSAR_AUDIO"
        startService(intent)
    }

    // Se ejecuta cuando el usuario vuelve a abrir la app
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        val intent = Intent(this, MusicaService::class.java)
        intent.action = "REANUDAR_AUDIO"
        startService(intent)
    }
}