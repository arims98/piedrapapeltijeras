package com.example.primeropiedra.View

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.example.primeropiedra.R
import androidx.activity.OnBackPressedCallback //Para que la flecha de la toolbar también funcione para volver atras dentro de la app

class Ayuda : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ayuda)

        //Configuramos la toolbar que ya tenemos en el xml
        val toolbar: Toolbar = findViewById(R.id.toolbarAyuda)
        setSupportActionBar(toolbar) //Para utilizar nuestra toolbar personalizada
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //Es una flecha para salir de la webview

        //Escuchamos la flecha de hacia atras del webview
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() //Aqui la funcion, que tenemos abajo, para que vaya hacia atras y no se cierre la app
        }

        webView = findViewById(R.id.webView)

        //Ajustes para facilitar la lectura y el diseño acorde
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true //Ajusta el contenido al ancho de la pantalla
        settings.useWideViewPort = true //Permite ver la web
        settings.builtInZoomControls = true //Permite amplicar el texto para verlo mejor
        settings.displayZoomControls = false //Oculta los controles de zoom nativo

        //Ahora forzamos para que los enlaces se abran dentro de al app y no en chrome

        webView.webViewClient = WebViewClient()

        //Carga de la info
        webView.loadUrl("file:///android_asset/ayuda.html")

        //Para que la flecha de la toolbar también funcione para volver atras dentro de la app
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack() // Si la web puede volver atrás, lo hace
                } else {
                    isEnabled = false // Desactiva este bloque
                    onBackPressedDispatcher.onBackPressed() // Cierra la actividad
                }
            }
        })
    }
    //Funcion para controlar  que el boton de antras del movil retroceda en la web y no cierra la app
    fun onBackPressedDispatcher() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressedDispatcher
        }
    }



}
