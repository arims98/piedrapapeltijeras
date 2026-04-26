package com.example.primeropiedra.View

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
        val btnVerMapa: ImageButton = view.findViewById(R.id.btnVerMapa)
    }

    var listaFiltrada: MutableList<PartidaTabla> = lista.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CajasHistorial {
        val vistaPuesta = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial, parent, false)
        return CajasHistorial(vistaPuesta)
    }

    override fun onBindViewHolder(holder: CajasHistorial, position: Int) {
        val partidaActual = listaFiltrada[position]
        val context = holder.itemView.context

        // Asignamos los datos básicos
        holder.tvNombre.text = partidaActual.nombre
        holder.tvFechaPartida.text = partidaActual.fecha
        holder.tvDuracion.text = "${context.getString(R.string.historial_tiempo)}${partidaActual.duracion}s"
        holder.tvMonedasTotal.text = "${partidaActual.monedas}"
        holder.tvMarcadorFinal.text = "${partidaActual.resultadoJugador} — ${partidaActual.resultadoIA}"

        // --- LÓGICA DEL BOTÓN DE MAPA (MOVIDA AQUÍ) ---
        holder.btnVerMapa.setOnClickListener {
            val ubi = partidaActual.ubicacion

            if (!ubi.isNullOrEmpty() && ubi != "Sin datos") {
                val gmmIntentUri = Uri.parse("geo:0,0?q=$ubi")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                // Intentamos abrir Maps, si falla, usamos el navegador
                try {
                    context.startActivity(mapIntent)
                } catch (e: Exception) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$ubi"))
                    context.startActivity(browserIntent)
                }
            }
        }

        // --- VISIBILIDAD DE MONEDAS ---
        if (esTOP) {
            holder.tvMonedasTotal.visibility = View.GONE
        } else {
            holder.tvMonedasTotal.visibility = View.VISIBLE
        }

        // --- TRADUCCIÓN DE VICTORIA / DERROTA ---
        if (partidaActual.resultadoJugador >= 5) {
            holder.tvResultadoTexto.text = context.getString(R.string.historial_victoria)
            holder.tvResultadoTexto.setTextColor(Color.parseColor("#1B5E20"))
            holder.viewColor.setBackgroundColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvResultadoTexto.text = context.getString(R.string.historial_derrota)
            holder.tvResultadoTexto.setTextColor(Color.parseColor("#B71C1C"))
            holder.viewColor.setBackgroundColor(Color.parseColor("#F44336"))
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
        notifyDataSetChanged()
    }
}