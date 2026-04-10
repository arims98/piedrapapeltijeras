package com.example.primeropiedra.Services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.media.MediaPlayer
import com.example.primeropiedra.R

class MusicaService : Service() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.sugar)
        mediaPlayer.isLooping = true //Para que la musica una vez acabé, vuelva a empezar
        mediaPlayer.setVolume(0.5f, 0.5f)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Hilo secundario automático: MediaPlayer ya gestiona su propio hilo de audio
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
        return START_STICKY // Para que el servicio no muera si la memoria baja
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}