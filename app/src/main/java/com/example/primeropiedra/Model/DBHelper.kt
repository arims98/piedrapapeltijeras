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
        db?.execSQL("CREATE TABLE Historial (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, monedas INTEGER, fecha TEXT, duracion INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Historial")
        onCreate(db)
    }

    // Funcion con RxJava
    // Usamos 'competable porque solo queremos guardar, no devolver datos ahora
    fun guardarPartidaAsync(
        nombre: String,
        monedas: Int,
        fecha: String,
        duracion: Int
    ): Completable {
        return Completable.fromAction {
            val db = this.writableDatabase
            val values = ContentValues().apply {
                put("nombre", nombre)
                put("monedas", monedas)
                put("fecha", fecha)
                put("duracion", duracion)
            }
            db.insert("Historial", null, values)
        }.subscribeOn(Schedulers.io()) // Esto hace que se guarde en segundo plano (Asíncrono)

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
                            duracion = it.getInt(it.getColumnIndexOrThrow("duracion"))
                        )
                        listaPartidas.add(partida)
                    } while (it.moveToNext())
                }
            }
            // .toList() convierte la MutableList en la List que espera el Single
            listaPartidas.toList()
        }.subscribeOn(Schedulers.io())
    }
}