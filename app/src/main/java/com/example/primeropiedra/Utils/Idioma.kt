package com.example.primeropiedra.Utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object Idioma {
    fun cambiar(context: Context, codigoIdioma: String) {
        val locale = Locale(codigoIdioma)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        // Esto actualiza los recursos de la app al nuevo idioma
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Guardamos la elección para que al cerrar la app no se pierda
        val prefs = context.getSharedPreferences("AjustesApp", Context.MODE_PRIVATE)
        prefs.edit().putString("idioma_seleccionado", codigoIdioma).apply()
    }
}