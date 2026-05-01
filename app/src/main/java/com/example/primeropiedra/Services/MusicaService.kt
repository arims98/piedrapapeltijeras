package com.example.primeropiedra.Services

import android.content.*
import android.media.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.primeropiedra.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log

class MusicaService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private var focusRequest: AudioFocusRequest? = null

    // Este listener controla lo que pasa cuando Spotify, una alarma o llamada interviene
    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Pérdida permanente (ej: otra app toma el control total)
                detenerMusica()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pérdida temporal (ej: una llamada o notificación de alarma)
                mediaPlayer?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Notificación corta (ej: sonido de WhatsApp). Bajamos volumen.
                mediaPlayer?.setVolume(0.1f, 0.1f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Recuperamos el foco (ej: colgamos llamada o Spotify se para)
                mediaPlayer?.setVolume(0.5f, 0.5f)
                mediaPlayer?.start()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        prepararMediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "REANUDAR_AUDIO" -> iniciarMusica()
            "PAUSAR_AUDIO" -> mediaPlayer?.pause()
            "RECARGAR_MUSICA" -> prepararMediaPlayer() // Por si cambias de canción
        }
        return START_STICKY
    }

    private fun prepararMediaPlayer() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null

            val prefs = getSharedPreferences("AjustesApp", Context.MODE_PRIVATE)
            val uriString = prefs.getString("uri_personalizada", null)

            mediaPlayer = if (uriString != null) {
                MediaPlayer().apply {
                    setDataSource(applicationContext, Uri.parse(uriString))
                    prepare()
                }
            } else {
                MediaPlayer.create(this, R.raw.sugar)
            }

            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(0.5f, 0.5f)
        } catch (e: Exception) {
            Log.e("MUSICA", "Error cargando música: ${e.message}")
        }
    }

    private fun iniciarMusica() {
        if (mediaPlayer == null) prepararMediaPlayer()

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(afChangeListener)
                .build()

            audioManager.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer?.start()
        }
    }

    private fun detenerMusica() {
        mediaPlayer?.pause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
            audioManager.abandonAudioFocusRequest(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(afChangeListener)
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}