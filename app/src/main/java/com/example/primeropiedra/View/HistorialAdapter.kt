package com.example.primeropiedra.View

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.primeropiedra.Model.PartidaTabla
import com.example.primeropiedra.R

class HistorialAdapter(private val lista: List<PartidaTabla>) :
    RecyclerView.Adapter<HistorialAdapter.CajasHistorial>() {

    class CajasHistorial(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreHistorial)
        val tvResultadoTexto: TextView = view.findViewById(R.id.tvResultadoTexto)
        val tvMonedasTotal: TextView = view.findViewById(R.id.tvMonedasTotal)
        val tvFechaPartida: TextView = view.findViewById(R.id.tvFechaPartida)
        val tvDuracion: TextView = view.findViewById(R.id.tvDuracion)
        val tvMarcadorFinal: TextView = view.findViewById(R.id.tvMarcadorFinal)
        val viewColor: View = view.findViewById(R.id.viewResultadoColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CajasHistorial {
        val vistaPuesta = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return CajasHistorial(vistaPuesta)
    }

    override fun onBindViewHolder(holder: CajasHistorial, position: Int) {
        val partidaActual = lista[position]

        // Asignamos los datos
        holder.tvNombre.text = partidaActual.nombre.uppercase()
        holder.tvFechaPartida.text = partidaActual.fecha
        holder.tvDuracion.text = "Tiempo: ${partidaActual.duracion}s"
        holder.tvMonedasTotal.text = "${partidaActual.monedas}"

        // Unimos el marcador en una sola línea
        holder.tvMarcadorFinal.text = "${partidaActual.resultadoJugador} — ${partidaActual.resultadoIA}"

        // Lógica de Victoria/Derrota (A 5 victorias)
        if (partidaActual.resultadoJugador >= 5) {
            holder.tvResultadoTexto.text = "¡VICTORIA!"
            holder.tvResultadoTexto.setTextColor(Color.parseColor("#1B5E20")) // Verde oscuro
            holder.viewColor.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde brillante
        } else {
            holder.tvResultadoTexto.text = "DERROTA"
            holder.tvResultadoTexto.setTextColor(Color.parseColor("#B71C1C")) // Rojo oscuro
            holder.viewColor.setBackgroundColor(Color.parseColor("#F44336")) // Rojo brillante
        }
    }

    override fun getItemCount(): Int = lista.size
}