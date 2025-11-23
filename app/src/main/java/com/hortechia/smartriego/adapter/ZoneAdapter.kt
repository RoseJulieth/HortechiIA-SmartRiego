package com.hortechia.smartriego.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hortechia.smartriego.R
import com.hortechia.smartriego.model.Zone

/**
 * Adapter para el RecyclerView de zonas en el Dashboard
 *
 * @param zones Lista de zonas a mostrar
 * @param onZoneClick Callback cuando se hace click en una zona
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
class ZoneAdapter(
    private val zones: List<Zone>,
    private val onZoneClick: (Zone) -> Unit
) : RecyclerView.Adapter<ZoneAdapter.ZoneViewHolder>() {

    inner class ZoneViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivZoneIcon: ImageView = view.findViewById(R.id.ivZoneIcon)
        val tvZoneName: TextView = view.findViewById(R.id.tvZoneName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val viewStatusIndicator: View = view.findViewById(R.id.viewStatusIndicator)
        val tvHumidity: TextView = view.findViewById(R.id.tvHumidity)
        val tvTemperature: TextView = view.findViewById(R.id.tvTemperature)

        fun bind(zone: Zone) {
            ivZoneIcon.setImageResource(zone.icon)
            tvZoneName.text = zone.name
            tvStatus.text = zone.getStatusText()
            tvHumidity.text = zone.getHumidityText()
            tvTemperature.text = zone.getTemperatureText()

            // Cambiar color del indicador según estado
            viewStatusIndicator.setBackgroundResource(
                if (zone.isActive) R.drawable.indicator_active
                else R.drawable.indicator_inactive
            )

            // Click listener
            itemView.setOnClickListener {
                onZoneClick(zone)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_zone_card, parent, false)
        return ZoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: ZoneViewHolder, position: Int) {
        holder.bind(zones[position])
    }

    override fun getItemCount(): Int = zones.size
}