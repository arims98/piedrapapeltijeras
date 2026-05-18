package com.example.primeropiedra.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * 1. EL MODELO DE DATOS (Moshi)
 * ¿Por qué? Internet solo entiende texto plano (JSON). Moshi traduce automáticamente
 * ese texto a esta clase de Kotlin para que podamos usar .nombre o .victorias fácilmente.
 */
@JsonClass(generateAdapter = true)
data class JugadorPuntuacion(
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "victorias") val victorias: Int = 0,
    @Json(name = "uid") val uid: String = ""
)

/**
 * 2. LA INTERFAZ DE LLAMADAS (Retrofit)
 * ¿Por qué? Aquí le decimos a Retrofit qué queremos hacer en la web.
 * - @GET: Para descargar la lista de puntuaciones.
 * - @PUT: Para guardar o actualizar los datos de un jugador usando su ID único (uid).
 */
interface FirebaseRestApi {
    @GET("puntuaciones.json")
    suspend fun obtenerPuntuaciones(): Map<String, JugadorPuntuacion>?

    @PUT("puntuaciones/{uid}.json")
    suspend fun actualizarPuntuacion(
        @Path("uid") uid: String,
        @Body datos: JugadorPuntuacion
    ): JugadorPuntuacion
}

/**
 * 3. EL CLIENTE DE CONEXIÓN (Singleton)
 * ¿Por qué? No queremos que la app cree una conexión a internet nueva cada vez que guarde un dato
 * porque consumiría toda la batería y CPU. Con 'object' creamos una única instancia para toda la app.
 */
object RetrofitClient {

    // !!! PEGA AQUÍ TU URL !!!
    // Borra esta de abajo y pon la URL exacta que copiaste de tu pestaña "Datos" de Firebase
    private const val BASE_URL = "https://primero-piedra-default-rtdb.europe-west1.firebasedatabase.app/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // Permite que Moshi entienda Kotlin a la perfección
        .build()

    val instancia: FirebaseRestApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // Acoplamos Moshi a Retrofit
            .build()
            .create(FirebaseRestApi::class.java)
    }
}

