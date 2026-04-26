package com.example.primeropiedra.View

import android.content.Intent
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
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.graphics.Color
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.example.primeropiedra.Model.DBHelper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import androidx.recyclerview.widget.LinearLayoutManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.primeropiedra.Services.MusicaService
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.provider.CalendarContract
import androidx.core.app.NotificationCompat

class JuegoView : AppCompatActivity() {
    var nombreJugador: String = ""
    private val viewModel: JuegoViewModel by viewModels()
    lateinit var btnPiedra: ImageView
    lateinit var btnPapel: ImageView
    lateinit var btnTijeras: ImageView
    lateinit var btnStart: Button
    lateinit var sacoMonedas: TextView
    lateinit var marcador: TextView
    lateinit var manoPC: ImageView
    lateinit var Mensajes: TextView
    private var segundosTranscurridos = 0
    private var runnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    lateinit var historialAdapter: HistorialAdapter
    private lateinit var rvResultados: RecyclerView
    private lateinit var dbHelper: DBHelper
    private lateinit var soundPool: SoundPool //Para reproducir sonidos rápidos y que no sea lento
    private var sonidopiedraId: Int = 0
    private var sonidopapelId: Int = 0
    private var sonidotijerasId: Int = 0
    private lateinit var fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.juego)

        // 1. Abrimos la libreta de SharedPreferences
        val prefs = getSharedPreferences("DatosJugador", Context.MODE_PRIVATE)

        // 2. Intentamos recuperar el nombre del Intent.
        // SI es nulo (porque hemos cambiado de idioma), lo buscamos en la libreta.
        // SI no está en la libreta, entonces ponemos "Invitado".
        val nombreRecuperado = intent.getStringExtra("nombreUsuario")
            ?: prefs.getString("nombre_guardado", null)
            ?: "Invitado"

        // 3. Si el nombre que hemos conseguido NO es "Invitado", lo guardamos/actualizamos
        if (nombreRecuperado != "Invitado") {
            val editor = prefs.edit()
            editor.putString("nombre_guardado", nombreRecuperado)
            editor.apply()
        }

        // 4. Le pasamos el nombre (el de verdad) al ViewModel
        viewModel.setNombreJugador(nombreRecuperado, this)
        viewModel.iniBaseDatos(this)

        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this) //Ubicación

        // Vincular vistas
        marcador = findViewById(R.id.marcador)
        manoPC = findViewById(R.id.manoPC)
        btnStart = findViewById(R.id.btnStart)
        btnPiedra = findViewById(R.id.piedraJugador)
        btnPapel = findViewById(R.id.papelJugador)
        btnTijeras = findViewById(R.id.tijerasJugador)
        sacoMonedas = findViewById(R.id.sacoMonedas)
        Mensajes = findViewById(R.id.Mensajes)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        desactivamosManos()

        observamosDatos()

        mostrarmensajejuego()

        dbHelper = DBHelper(this)
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
                Log.e(
                    "ErrorDB",
                    "No se pudieron cargar los datos para el buscador: ${error.message}"
                )
            })

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10) // Permite sonar 10 sonidos a la vez sin cortarse
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                Log.d("SoundPool", "Sonido cargado con éxito, ID: $sampleId")
            } else {
                Log.e("SoundPool", "Error al cargar el sonido")
            }
        }

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(
                android.Manifest.permission.WRITE_CALENDAR,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ), 101)
        }

// Cargamos los sonidos
        sonidopiedraId = soundPool.load(this, R.raw.stone, 1)
        sonidopapelId = soundPool.load(this, R.raw.paper, 1)
        sonidotijerasId = soundPool.load(this, R.raw.tijeras, 1)

        btnStart.setOnClickListener {
            activamosManos()
            viewModel.iniciarPartida()
            viewModel.iniciarCronometro()
            iniciarVisualCrono()
            btnStart.visibility = View.GONE
            Toast.makeText(this, getString(R.string.toast_partidainiciada), Toast.LENGTH_SHORT).show()
        }

        btnPiedra.setOnClickListener {
            sonarMano("piedra")
            jugar(1)
        }
        btnPapel.setOnClickListener {
            jugar(2)
            sonarMano("papel")
        }
        btnTijeras.setOnClickListener {
            jugar(3)
            sonarMano("tijeras")
        }

        viewModel.mensajeEstado.observe(this) { mensaje ->
            if (mensaje == "NOTIFICACION_VICTORIA") {
                // 1. Obtenemos los puntos reales del ViewModel o de tus variables de la clase
                val puntosFinalesJugador = viewModel.victoriasJugador.value ?: 5
                val puntosFinalesIA = viewModel.victoriasIA.value ?: 0

                // 2. Pasamos los datos reales a la función del calendario
                guardarvictoriacalendar(puntosFinalesJugador, puntosFinalesIA)
                Mensajes.text = getString(R.string.msg_guardado_ok)
            } else {
                val textoTraducido = when {
                    mensaje == "¡Empate en esta ronda!" -> getString(R.string.msg_empate)
                    mensaje == "La máquina gana..." -> getString(R.string.msg_punto_ia)

                    // SI EL MENSAJE EMPIEZA POR "¡Punto para", LO TRADUCIMOS
                    mensaje.startsWith("¡Punto para") -> {
                        // Sacamos el nombre (quitamos la parte de "¡Punto para ")
                        val nombre = mensaje.replace("¡Punto para ", "").replace("!", "")
                        getString(R.string.msg_punto_jugador) + nombre + "!"
                    }

                    else -> mensaje
                }

                Mensajes.text = textoTraducido
            }

            Mensajes.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                Mensajes.visibility = View.INVISIBLE
            }, 2000)
        }

    }

    fun jugar(eleccionJugador: Int) {
        desactivamosManos()
        viewModel.jugar(eleccionJugador)

        val vJugador = viewModel.victoriasJugador.value ?: 0
        val vIA = viewModel.victoriasIA.value ?: 0

        if (vJugador >= 5 || vIA >= 5) {
            finalizarPartida()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            manoPC.visibility = View.INVISIBLE
            if (vJugador < 5 && vIA < 5) {
                activamosManos()
            }
        }, 2000)
    }

    fun finalizarPartida() {
        detenerCronoVisual()
        viewModel.calcularResultadoFinal()
        desactivamosManos()

        // --- REQUISITO: OBTENER UBICACIÓN ANTES DE GUARDAR ---
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val ubiString = if (location != null) "${location.latitude},${location.longitude}" else "Sin datos"

                // Le pasamos la ubicación al motor (ViewModel)
                viewModel.setUbicacion(ubiString)

                // Ahora sí, guardamos la partida con todo
                val segundos = viewModel.obtenerDuracionSegundos()
                val fechaHoy = obtenerFechaActual()
                viewModel.registrarPartidaBD(segundos, fechaHoy)

                Log.d("UBICACION", "Guardando con coordenadas: $ubiString")
            }
        } else {
            // Si no hay permiso, guardamos indicando que no hay permiso
            viewModel.setUbicacion("Permiso denegado")
            viewModel.registrarPartidaBD(viewModel.obtenerDuracionSegundos(), obtenerFechaActual())
        }

        // --- LANZAMOS EL CARTEL DE RESULTADO ---
        val ganoJugador = (viewModel.victoriasJugador.value ?: 0) >= 5
        mostrarCartelFinal(
            victoria = ganoJugador,
            puntosJugador = viewModel.victoriasJugador.value ?: 0,
            puntosIA = viewModel.victoriasIA.value ?: 0
        )
    }

    private fun mostrarCartelFinal(victoria: Boolean, puntosJugador: Int, puntosIA: Int) {
        val builder = AlertDialog.Builder(this)
        val monedasActuales = viewModel.monedas.value ?: 0

        val mensajeCompleto = if (victoria) {

            builder.setTitle(getString(R.string.titulo_victoria))
            builder.setIcon(android.R.drawable.btn_star_big_on)


            getString(R.string.msg_enhorabuena) + "\n\n" +
                    getString(R.string.msg_has_ganado) + "$puntosJugador a $puntosIA.\n" +
                    getString(R.string.msg_ahora_tienes) + "$monedasActuales" + getString(R.string.msg_monedas_excl) + "\n" +
                    getString(R.string.msg_tiempo_de) + "$segundosTranscurridos" + getString(R.string.msg_segundos) + "\n\n" +
                    getString(R.string.msg_que_hacer)

        } else {

            builder.setTitle(getString(R.string.titulo_derrota))
            builder.setIcon(android.R.drawable.ic_delete)


            getString(R.string.msg_lo_siento) + "\n\n" +
                    getString(R.string.msg_ia_ganado) + "$puntosIA a $puntosJugador.\n" +
                    getString(R.string.msg_ahora_tienes) + "$monedasActuales monedas...\n" +
                    getString(R.string.msg_tiempo_de) + "$segundosTranscurridos" + getString(R.string.msg_segundos) + "\n\n" +
                    getString(R.string.msg_practicando) + "\n" +
                    getString(R.string.msg_que_hacer)
        }

        builder.setMessage(mensajeCompleto)

        builder.setPositiveButton(getString(R.string.btn_reintentar)) { _, _ ->
            // Simulamos el click del botón start para reiniciar todo
            btnStart.performClick()
        }

        builder.setNeutralButton(getString(R.string.btn_captura)) { _, _ ->
            val nombreArchivo =
                "Resultado_PiedraPapelTijeras_${System.currentTimeMillis()}.png"
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/png"
                putExtra(Intent.EXTRA_TITLE, nombreArchivo)
            }
            startActivityForResult(intent, 1001)
        }

        builder.setNegativeButton(getString(R.string.btn_historial)) { _, _ ->
            val intent = Intent(this, HistorialView::class.java)
            startActivity(intent)
            finish()
        }

        val dialog = builder.create()
        dialog.setCancelable(false) // Obligamos a elegir una opción
        dialog.show()
    }

    fun desactivamosManos() {
        btnPiedra.isEnabled = false
        btnPapel.isEnabled = false
        btnTijeras.isEnabled = false
        btnPapel.alpha = 0.5f
        btnPiedra.alpha = 0.5f
        btnTijeras.alpha = 0.5f
    }

    fun activamosManos() {
        btnPiedra.isEnabled = true
        btnPapel.isEnabled = true
        btnTijeras.isEnabled = true
        btnPapel.alpha = 1.0f
        btnPiedra.alpha = 1.0f
        btnTijeras.alpha = 1.0f
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

            else -> super.onOptionsItemSelected(item) //Aqui debo poner para ir a la pagina de login
        }
    }

    private fun observamosDatos() {
        viewModel.Marcador.observe(this) { nuevoMarcador ->
            marcador.text = nuevoMarcador
        }
        viewModel.monedas.observe(this) { cantidad ->
            sacoMonedas.text = cantidad.toString()
        }
        viewModel.eleccionIA.observe(this) { eleccionIA ->
            manoPC.visibility = View.VISIBLE
            when (eleccionIA) {
                1 -> {
                    manoPC.setImageResource(R.drawable.descargapiedra)
                    sonarMano("piedra")
                }

                2 -> {
                    manoPC.setImageResource(R.drawable.descargapapel)
                    sonarMano("papel")
                }

                3 -> {
                    manoPC.setImageResource(R.drawable.descargatijeras)
                    sonarMano("tijeras")
                }
            }
        }
    }

    fun obtenerFechaActual(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    fun iniciarVisualCrono() {
        // 1. Traducimos la etiqueta de Fecha
        val etiquetaFecha = getString(R.string.txt_fecha_label)
        findViewById<TextView>(R.id.tvFechaSesion).text = "$etiquetaFecha ${obtenerFechaActual()}"

        segundosTranscurridos = 0
        runnable = object : Runnable {
            override fun run() {
                segundosTranscurridos++
                val minutos = segundosTranscurridos / 60
                val segundos = segundosTranscurridos % 60

                // 2. Traducimos la etiqueta de Tiempo
                val etiquetaTiempo = getString(R.string.txt_tiempo_label)

                // Formateamos los números (00:00)
                val tiempoFormateado = String.format("%02d:%02d", minutos, segundos)

                // Unimos: "Tiempo: " + "00:01"
                findViewById<TextView>(R.id.tvCronometro).text = "$etiquetaTiempo $tiempoFormateado"

                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun detenerCronoVisual() {
        runnable?.let { handler.removeCallbacks(it) }
    }

    private fun mostrarmensajejuego() {
        val builder = android.app.AlertDialog.Builder(this)

        // Título traducido
        builder.setTitle(getString(R.string.titulo_empieza))

        // Mensaje traducido
        builder.setMessage(getString(R.string.msg_objetivo))

        val dialog = builder.create()
        dialog.show()

        // PROGRAMAMOS EL CIERRE AUTOMÁTICO
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 2000)
    }

    private fun sonarMano(tipo: String) {
        val sonidoSeleccionado = when (tipo.lowercase()) {
            "piedra" -> sonidopiedraId
            "papel" -> sonidopapelId
            "tijeras" -> sonidotijerasId
            else -> 0
        }
        if (sonidoSeleccionado != 0) {
            soundPool.play(sonidoSeleccionado, 1f, 1f, 1, 0, 1f)
        }
    }

    private fun lanzarNotificacionVictoria() {
        Log.d("PRUEBA", "ENTRANDO EN LA FUNCIÓN DE NOTIFICACIÓN")
        val canalId = "canal_victorias"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // IMPORTANTE: El nombre del canal (segundo parámetro) es lo que ve el usuario en ajustes
            val canal = NotificationChannel(
                canalId, "Resultados de Partida",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(canal)
        }

        val builder = NotificationCompat.Builder(this@JuegoView, canalId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Usa este del sistema para asegurar que no crashea
            .setContentTitle("¡Victoria!")
            .setContentText("Tu partida se ha guardado correctamente en el historial.") // OBLIGATORIO
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(101, builder.build())
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) { //Función que se activa auto cuando el explorador de archivos se cierra
        //requestCode -> el numero matricula
        //resultCode -> si el usuario aceptop o canceló
        //data -> contiene la dirección URI del lugar elegido para guardar el archivo
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        ) //Llama a la funcion base de android para que siga el curso

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            data?.data?.let { uri -> //Extrae la uri la direccion fisica del  movil y con el let, para saber si la dirección existe y no es nula, llama a uri y hacemos lo siguiente
                //Escudo de seguridad, si el movil se queda sin memoria, etc el catch atra el error y asi la app no se crashea
                try {
                    // Obtenemos la foto que hicimos antes
                    val bitmap = capturaPantalla()

                    // Abrimos el "tunel" hacia la ubicación elegida por el usuario
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        // Formato óptimo: PNG (sin pérdida de calidad)
                        bitmap.compress(
                            android.graphics.Bitmap.CompressFormat.PNG,
                            100,
                            outputStream
                        )
                        //Toast.makeText(this, "Imagen guardada con éxito", Toast.LENGTH_SHORT).show()

                    }
                } catch (e: Exception) {
                    Log.e("Captura", "Error al guardar: ${e.message}")
                    Toast.makeText(this, getString(R.string.toast_error_partidaguardada), Toast.LENGTH_SHORT).show()
                }
            }

        }

    }
    private fun capturaPantalla(): android.graphics.Bitmap {
        val view = window.decorView.rootView
        val bitmap = android.graphics.Bitmap.createBitmap(
            view.width,
            view.height,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    private fun guardarvictoriacalendar(puntosJugador: Int, puntosIA: Int) {
        //Usamos hilos secundarios para no bloquear la CPU
        Thread{
            try {
                val cr = contentResolver
                val values = ContentValues()

                //Dia y hora correcta
                val startMillis: Long = System.currentTimeMillis()
                val endMillis: Long = startMillis + (10 * 60 * 1000)

                // 2. Configuramos los datos del evento
                values.put(CalendarContract.Events.DTSTART, startMillis)
                values.put(CalendarContract.Events.DTEND, endMillis)
                values.put(CalendarContract.Events.TITLE, "Juego PPT: ¡Victoria!")
                values.put(CalendarContract.Events.DESCRIPTION, "Ganaste a la IA $puntosJugador a $puntosIA")

                // Usamos el ID 1 (calendario principal por defecto en el 99% de Android)
                values.put(CalendarContract.Events.CALENDAR_ID, 1)
                values.put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().id)

                // 3. REQUISITO: Acceso mediante Content Provider
                val uri = cr.insert(CalendarContract.Events.CONTENT_URI, values)

                if (uri != null) {
                    Log.d("CALENDARIO", "Éxito: Victoria guardada en el calendario del sistema")
                }
            } catch (e: Exception) {
                Log.e("CALENDARIO", "Error de permisos o de inserción: ${e.message}")
            }
        }.start()
    }
}





