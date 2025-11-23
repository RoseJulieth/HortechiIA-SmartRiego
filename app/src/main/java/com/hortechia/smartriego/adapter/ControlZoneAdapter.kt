package com.hortechia.smartriego.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.hortechia.smartriego.R
import com.hortechia.smartriego.model.Zone

/**
 * Adapter para el RecyclerView de Control Manual
 *
 * @param zones Lista de zonas
 * @param onStatusChanged Callback cuando cambia el estado del switch
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
class ControlZoneAdapter(
    private val zones: List<Zone>,
    private val onStatusChanged: (Zone, Boolean) -> Unit
) : RecyclerView.Adapter<ControlZoneAdapter.ZoneViewHolder>() {

    inner class ZoneViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivZoneIcon: ImageView = view.findViewById(R.id.ivZoneIcon)
        val tvZoneName: TextView = view.findViewById(R.id.tvZoneName)
        val switchStatus: SwitchMaterial = view.findViewById(R.id.switchStatus)
        val viewStatusIndicator: View = view.findViewById(R.id.viewStatusIndicator)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvLastIrrigation: TextView = view.findViewById(R.id.tvLastIrrigation)

        fun bind(zone: Zone) {
            ivZoneIcon.setImageResource(zone.icon)
            tvZoneName.text = zone.name
            switchStatus.isChecked = zone.isActive

            updateStatus(zone.isActive)

            val lastIrrigationText = "Última activación: ${zone.getLastIrrigationText()}"
            tvLastIrrigation.text = lastIrrigationText

            // Listener del switch
            switchStatus.setOnCheckedChangeListener { _, isChecked ->
                updateStatus(isChecked)
                onStatusChanged(zone, isChecked)
            }
        }

        private fun updateStatus(isActive: Boolean) {
            if (isActive) {
                viewStatusIndicator.setBackgroundResource(R.drawable.indicator_active)
                tvStatus.text = itemView.context.getString(R.string.control_manual_on)
            } else {
                viewStatusIndicator.setBackgroundResource(R.drawable.indicator_inactive)
                tvStatus.text = itemView.context.getString(R.string.control_manual_off)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_control_zone, parent, false)
        return ZoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: ZoneViewHolder, position: Int) {
        holder.bind(zones[position])
    }

    override fun getItemCount(): Int = zones.size
}