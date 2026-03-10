/* ¿Qué es el ViewModel de verdad?
Imagina que el XML es la carrocería del coche (lo que ves) y el Juego.kt es el salpicadero (los botones que tocas).

El ViewModel es el MOTOR. El motor no se ve, está escondido bajo el capó.

Si tú pisas el acelerador (botón en Juego.kt), el motor (ViewModel) hace la explosión y decide a qué velocidad vas.

El salpicadero (Juego.kt) solo te enseña una aguja moviéndose, pero no sabe por qué se mueve. El motor es el que tiene la fuerza y los datos.

En Android, se inventó el ViewModel por un problema de "memoria". Si tú giras el móvil, Android destruye y vuelve a crear Juego.kt. Si tus monedas estaban ahí, ¡pum!, desaparecen.
El ViewModel es una "caja fuerte" que sobrevive a ese giro. Por eso el profesor lo pide: porque es la forma profesional de que no se pierdan los datos.*/

package com.example.primeropiedra.ViewModel //Decimos la ubicacion de la carpeta

import androidx.lifecycle.MutableLiveData
// Imagina que MutableLiveData es una "pizarra mágica" que permite que otros (la pantalla) vean cuando el valor cambia.
// Si no lo importas, Android no sabe qué es esa pizarra.

import androidx.lifecycle.ViewModel //Importamos la "caja fuerte" básica de Android sobre la que vamos a construir nuestro motor.


class JuegoViewModel : ViewModel() { // Aquí creamos nuestra clase. Al poner los dos puntos y ViewModel(), //
// le decimos a Android: "Hereda todas las funciones de una caja fuerte". Esto es lo que permite que los datos no se borren al girar el móvil.
}