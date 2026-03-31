package com.example.primeropiedra.View

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.primeropiedra.Model.PartidaTabla
import com.example.primeropiedra.R

class HistorialAdapter(val lista: List<PartidaTabla>,
    private val esTOP: Boolean = false) :
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
    var listaFiltrada: MutableList<PartidaTabla> = lista.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CajasHistorial {
        val vistaPuesta = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return CajasHistorial(vistaPuesta)
    }

    override fun onBindViewHolder(holder: CajasHistorial, position: Int) {
        val partidaActual = listaFiltrada[position]

        // Asignamos los datos
        holder.tvNombre.text = partidaActual.nombre
        holder.tvFechaPartida.text = partidaActual.fecha
        holder.tvDuracion.text = "Tiempo: ${partidaActual.duracion}s"
        holder.tvMonedasTotal.text = "${partidaActual.monedas}"

        // Unimos el marcador en una sola línea
        holder.tvMarcadorFinal.text = "${partidaActual.resultadoJugador} — ${partidaActual.resultadoIA}"

        // --- LÓGICA PARA MOSTRAR O ESCONDER MONEDAS ---
        if (esTOP) {
            holder.tvMonedasTotal.visibility = View.GONE
            // Si tienes un icono de moneda, también: holder.ivMonedaIcono.visibility = View.GONE
        } else {
            holder.tvMonedasTotal.visibility = View.VISIBLE
            holder.tvMonedasTotal.text = "${partidaActual.monedas}"
        }

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

    override fun getItemCount(): Int = listaFiltrada.size

    fun filtrar(texto: String) {
        listaFiltrada.clear()
        if (texto.isEmpty()) {
            listaFiltrada.addAll(lista)
        } else {
            val busqueda = texto.lowercase()
            for (partida in lista) {
                if (partida.nombre.lowercase().contains(busqueda)) {
                    listaFiltrada.add(partida)
                }
            }
        }
        notifyDataSetChanged() // Refresca la lista en pantalla
    }
}