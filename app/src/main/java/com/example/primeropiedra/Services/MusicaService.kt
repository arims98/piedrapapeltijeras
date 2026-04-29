package com.example.primeropiedra.Services

import android.app.Service
import android.content.*
import android.media.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.primeropiedra.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.net.Uri

class MusicaService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private var focusRequest: AudioFocusRequest? = null

    // Variable para saber si nosotros pausamos por salir de la app (onPause)
    // o si el sistema nos pausó por una llamada.
    private var pausadoPorApp = false

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Si entra una llamada o alarma, pausamos
                mediaPlayer?.pause()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Cuando la otra app suelta el audio (cierras Spotify o cuelgas)
                val prefs = getSharedPreferences("AjustesApp", Context.MODE_PRIVATE)
                if (prefs.getBoolean("musica_viva", true) && !pausadoPorApp) {
                    mediaPlayer?.start()
                }
            }
            // Caso: Notificación corta. BAJAR VOLUMEN (Duck).
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.1f, 0.1f)
            }
            // Caso: Cuelgas la llamada o Spotify se quita. RECUPERAR.
            AudioManager.AUDIOFOCUS_GAIN -> {
                val prefs = getSharedPreferences("AjustesApp", Context.MODE_PRIVATE)
                val musicaActivada = prefs.getBoolean("musica_viva", true)

                // Solo reanudamos si el usuario quiere música Y si no fuimos nosotros
                // quienes la pausamos al salir de la pantalla (onPause).
                if (musicaActivada && !pausadoPorApp) {
                    mediaPlayer?.setVolume(0.5f, 0.5f)
                    mediaPlayer?.start()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        iniciarNotificacionPersistente()
        val prefs = getSharedPreferences("AjustesApp", Context.MODE_PRIVATE)
        val musicaActivada = prefs.getBoolean("musica_viva", true)

        when (intent?.action) {
            "REANUDAR_AUDIO" -> {
                pausadoPorApp = false
                if (musicaActivada) {
                    if (mediaPlayer == null) prepararMediaPlayer()
                    if (solicitarEnfoqueAudio()) mediaPlayer?.start()
                }
            }
            "PAUSAR_AUDIO" -> {
                pausadoPorApp = true // Marcamos que salimos de la app
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                }
            }
            "REINICIAR_AUDIO" -> {
                prepararMediaPlayer()
                if (musicaActivada && solicitarEnfoqueAudio()) mediaPlayer?.start()
            }
            else -> {
                // Al abrir la APP (Primer inicio)
                if (musicaActivada) {
                    pausadoPorApp = false
                    prepararMediaPlayer()
                    if (solicitarEnfoqueAudio()) mediaPlayer?.start()
                }
            }
        }
        return START_STICKY
    }

    private fun prepararMediaPlayer() {
        try {
            mediaPlayer?.release()
            val prefs = getSharedPreferences("AjustesApp", Context.MODE_PRIVATE)
            val uriString = prefs.getString("uri_personalizada", null)

            if (uriString != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(applicationContext, Uri.parse(uriString))
                    isLooping = true
                    prepare()
                }
            } else {
                mediaPlayer = MediaPlayer.create(this, R.raw.sugar)
                mediaPlayer?.isLooping = true
            }
            mediaPlayer?.setVolume(0.5f, 0.5f)
        } catch (e: Exception) {
            Log.e("MUSICA", "Error: ${e.message}")
        }
    }

    private fun solicitarEnfoqueAudio(): Boolean {
        // Verificamos si ya hay música sonando en el sistema (Spotify, etc.)
        // Si hay música de otra app, NO pedimos el foco para no cortarla
        if (audioManager.isMusicActive && !mediaPlayer!!.isPlaying) {
            Log.d("MUSICA", "Spotify u otra app está sonando. Me quedo callado.")
            return false
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true) // Esto es clave
                .setOnAudioFocusChangeListener(afChangeListener)
                .build()
            audioManager.requestAudioFocus(focusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun iniciarNotificacionPersistente() {
        val canalId = "musica_canal"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(canalId, "Música de fondo", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(canal)
        }
        val notification = NotificationCompat.Builder(this, canalId)
            .setContentTitle("Piedra, Papel, Tijera")
            .setContentText("Música activa")
            .setSmallIcon(R.drawable.music)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(1, notification)
    }
}