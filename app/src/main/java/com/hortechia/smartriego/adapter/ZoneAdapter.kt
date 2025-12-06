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
 * @param zones Lista MUTABLE de zonas a mostrar
 * @param onZoneClick Callback cuando se hace click en una zona
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
class ZoneAdapter(
    private var zones: MutableList<Zone>,
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

            viewStatusIndicator.setBackgroundResource(
                if (zone.isActive) R.drawable.indicator_active
                else R.drawable.indicator_inactive
            )

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
        val zone = zones[position]
        android.util.Log.d("ZoneAdapter", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        android.util.Log.d("ZoneAdapter", "onBindViewHolder - Position: $position")
        android.util.Log.d("ZoneAdapter", "  Zone ID: ${zone.id}")
        android.util.Log.d("ZoneAdapter", "  Zone Name: ${zone.name}")
        android.util.Log.d("ZoneAdapter", "  Humidity: ${zone.humidity}")
        android.util.Log.d("ZoneAdapter", "  Temperature: ${zone.temperature}")
        android.util.Log.d("ZoneAdapter", "  isActive: ${zone.isActive}")
        android.util.Log.d("ZoneAdapter", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        holder.bind(zone)
    }

    override fun getItemCount(): Int {
        android.util.Log.d("ZoneAdapter", "📊 getItemCount: ${zones.size} zonas")
        return zones.size
    }

    fun updateZones(newZones: List<Zone>) {
        zones.clear()
        zones.addAll(newZones)
        notifyDataSetChanged()
    }

    fun updateZone(position: Int, zone: Zone) {
        if (position in zones.indices) {
            zones[position] = zone
            notifyItemChanged(position)
        }
    }
}