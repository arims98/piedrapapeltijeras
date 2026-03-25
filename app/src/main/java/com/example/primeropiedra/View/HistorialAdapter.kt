package com.example.primeropiedra.View

import androidx.recyclerview.widget.RecyclerView
import com.example.primeropiedra.Model.PartidaTabla
import com.example.primeropiedra.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class HistorialAdapter(private  val lista: List<PartidaTabla>) :
    RecyclerView.Adapter<HistorialAdapter.CajasHistorial>() {

        // Creamos la clase cajasHistorial, para buscar los ids del xml
        class CajasHistorial (view: View) : RecyclerView.ViewHolder(view) {
            val nombre : TextView = view.findViewById(R.id.txtNombre)
            val monedas : TextView = view.findViewById(R.id.txtMonedas)
            val fechas: TextView = view.findViewById(R.id.txtFecha)
            val duracion: TextView = view.findViewById(R.id.txtduracion)
            val resultadoJugador: TextView = view.findViewById(R.id.minimarcadorJugador)
            val resultadoIA: TextView = view.findViewById(R.id.minimarcadorIA)
        }
    // Creamos la fila: infla el xml item_historial
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CajasHistorial {
        val vistaPuesta = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return CajasHistorial(vistaPuesta)
    }
    // Unimos la info de la base de datos con el xml
    override fun onBindViewHolder(holder: CajasHistorial, position: Int) {
        val partidaActual = lista[position]
        holder.nombre.text = "Jugador: ${partidaActual.nombre}"
        holder.monedas.text = "${partidaActual.monedas} \uD83D\uDCB0"
        holder.fechas.text = "Fecha: ${partidaActual.fecha}"
        holder.duracion.text = "Duracion: ${partidaActual.duracion}"
        holder.resultadoJugador.text = "Resultado: ${partidaActual.resultadoJugador}"
        holder.resultadoIA.text = "- ${partidaActual.resultadoIA}"
    }
    // Contamos las partidas que haya
    override fun getItemCount(): Int {
        return lista.size
    }
}