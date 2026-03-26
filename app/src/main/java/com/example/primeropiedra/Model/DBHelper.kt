// Esta carpeta, model, es una clase que decide si los datos se guardan en SQLite, usando RxJava
package com.example.primeropiedra.Model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.core.Single //Para ir a la base de datos y que traiga la lista y avise que termina

class DBHelper ( context: Context) : SQLiteOpenHelper(context, "JuegoDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        //Creamos la tabla
        db?.execSQL("CREATE TABLE Historial (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, monedas INTEGER, fecha TEXT, duracion INTEGER, resultadoJugador INTEGER, resultadoIA INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Historial")
        onCreate(db)
    }
    fun guardarPartidaAsync(partida: PartidaTabla): Completable {
        return Completable.fromAction {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put("nombre", partida.nombre)
                put("monedas", partida.monedas)
                put("fecha", partida.fecha)
                put("duracion", partida.duracion)
                put("resultadoJugador", partida.resultadoJugador)
                put("resultadoIA", partida.resultadoIA)
            }
            db.insert("Historial", null, values)
        }.subscribeOn(Schedulers.io())
    }
    fun obtenerHistorialAsync(): Single<List<PartidaTabla>> {
        return Single.fromCallable {
            val listaPartidas = mutableListOf<PartidaTabla>()
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM Historial ORDER BY id DESC", null)

            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val partida = PartidaTabla(
                            id = it.getInt(it.getColumnIndexOrThrow("id")),
                            nombre = it.getString(it.getColumnIndexOrThrow("nombre")),
                            monedas = it.getInt(it.getColumnIndexOrThrow("monedas")),
                            fecha = it.getString(it.getColumnIndexOrThrow("fecha")),
                            duracion = it.getInt(it.getColumnIndexOrThrow("duracion")),
                            resultadoJugador = it.getInt(it.getColumnIndexOrThrow("resultadoJugador")),
                            resultadoIA = it.getInt(it.getColumnIndexOrThrow("resultadoIA"))

                        )
                        listaPartidas.add(partida)
                    } while (it.moveToNext())
                }
            }
            // .toList() convierte la MutableList en la List que espera el Single
            listaPartidas.toList()
        }.subscribeOn(Schedulers.io())
    }
    fun obtenerHistorialTOP(): Single<List<PartidaTabla>> {
        return Single.fromCallable {
            val listaPartidas = mutableListOf<PartidaTabla>()
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT id, nombre, SUM(monedas) as Monedas, SUM(resultadoJugador) as totalJugador, SUM(resultadoIA) as totalMaquina, MAX(fecha) as ultimaFecha, SUM(duracion) as totalduracion FROM Historial GROUP BY nombre ORDER BY totalJugador DESC, Monedas DESC, totalduracion DESC LIMIT 3", null)

            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val partida = PartidaTabla(
                            id = it.getInt(it.getColumnIndexOrThrow("id")),
                            nombre = it.getString(it.getColumnIndexOrThrow("nombre")),
                            monedas = it.getInt(it.getColumnIndexOrThrow("Monedas")),
                            fecha = it.getString(it.getColumnIndexOrThrow("ultimaFecha")),
                            duracion = it.getInt(it.getColumnIndexOrThrow("totalduracion")),
                            resultadoJugador = it.getInt(it.getColumnIndexOrThrow("totalJugador")),
                            resultadoIA = it.getInt(it.getColumnIndexOrThrow("totalMaquina"))
                        )
                        listaPartidas.add(partida)
                    } while (it.moveToNext())
                }
            }
            // .toList() convierte la MutableList en la List que espera el Single
            listaPartidas.toList()
        }.subscribeOn(Schedulers.io())
    }
    // Busca la última partida de ese nombre y devuelve las monedas
    fun obtenerUltimasMonedas(nombreBusqueda: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT monedas FROM Historial WHERE nombre = ? ORDER BY id DESC LIMIT 1",
            arrayOf(nombreBusqueda)
        )

        var monedasEncontradas = 10 // Valor por defecto si es nuevo
        if (cursor.moveToFirst()) {
            monedasEncontradas = cursor.getInt(0)
        }
        cursor.close()
        return monedasEncontradas
    }


}