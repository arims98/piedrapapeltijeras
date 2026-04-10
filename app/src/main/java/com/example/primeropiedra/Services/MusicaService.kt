package com.example.primeropiedra.Services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.IBinder
import android.media.MediaPlayer
import android.os.Build
import com.example.primeropiedra.R
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

class MusicaService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager //Gestor del audio del sistema, para pedir o soltar el permiso de sonar
    private var focusRequest: AudioFocusRequest? = null //Guarda la solicitud oficial del audio
    //Es el escuchador de cambios, se queda escuchando lo que el sistema android le dice a mi app, sobre el sonido
    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            // Pausa temporal (Llamada, notificación, Spoti empieza)
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
            }

            // RECUPERAR EL SONIDO (Spotify se para)
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Subimos volumen y arrancamos. NO vuelvas a llamar a solicitarEnfoqueAudio() aquí.
                mediaPlayer.setVolume(0.5f, 0.5f)
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
            }

            // Pérdida total (Otra app de música toma el control permanente)
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaPlayer = MediaPlayer.create(this, R.raw.sugar)
        mediaPlayer.isLooping = true //Para que la musica una vez acabé, vuelva a empezar
        mediaPlayer.setVolume(0.5f, 0.5f)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        iniciarNotificacionPersistente()

        // Si recibimos la orden de reanudar desde una Activity
        if (intent?.action == "REANUDAR_AUDIO") {
            if (!mediaPlayer.isPlaying) {
                if (solicitarEnfoqueAudio()) {
                    mediaPlayer.start()
                }
            }
        } else {
            // Inicio normal del servicio
            if (solicitarEnfoqueAudio()) {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
            }
        }

        return START_STICKY
    }
    //Esta función devuelve un true si android nos deja sonar, false si no
    private fun solicitarEnfoqueAudio(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Le preguntamos a android si la version es 8(oreo) o superior
            val playbackAttributes = AudioAttributes.Builder() //Creamos como un sobre sobre la info de nuestro audio
                .setUsage(AudioAttributes.USAGE_GAME) //Le decimos al sistema que este sonido es para un juego
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) //Tambien le dice que el contenido es música, y hace ver a android que altavoz usar y que calidad dar
                .build() //Cerramos la confi de los atributos
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN) //Creamos la solicitud de foco, cy con el audiofocus_gain que el foco lo queremos por tiempo indefinido.
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(afChangeListener)
                .build()

            audioManager.requestAudioFocus(focusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            // Para versiones antiguas de Android
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }
    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null

    private fun iniciarNotificacionPersistente() {
        val canalId = "musica_canal"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Música de fondo"
            val importancia = NotificationManager.IMPORTANCE_LOW
            val canal = NotificationChannel(canalId, nombre, importancia)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }

        val notification = NotificationCompat.Builder(this, canalId)
            .setContentTitle("Piedra, Papel, Tijera")
            .setContentText("Disfrutando de la música...")
            .setSmallIcon(R.drawable.music) // Asegúrate de que este drawable existe
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(1, notification)
        }
    }


}




