// Esta carpeta, model, es una clase que decide si los datos se guardan en SQLite, usando RxJava
package com.example.primeropiedra.Model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

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
}