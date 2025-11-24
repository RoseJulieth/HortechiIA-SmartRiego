package com.hortechia.smartriego.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hortechia.smartriego.databinding.ItemProgramacionBinding
import com.hortechia.smartriego.models.ProgramacionData

class ProgramacionAdapter(
    private val programaciones: List<ProgramacionData>,
    private val onSwitchChanged: (ProgramacionData, Boolean) -> Unit,
    private val onEditClick: (ProgramacionData) -> Unit,
    private val onDeleteClick: (ProgramacionData) -> Unit
) : RecyclerView.Adapter<ProgramacionAdapter.ProgramacionViewHolder>() {

    inner class ProgramacionViewHolder(
        private val binding: ItemProgramacionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(programacion: ProgramacionData) {
            binding.apply {
                tvNombreProgramacion.text = programacion.nombre
                tvZona.text = programacion.zona
                tvHorario.text = programacion.hora
                tvDuracion.text = "${programacion.duracion} minutos"
                tvProximaEjecucion.text = "Próxima ejecución: ${programacion.proximaEjecucion}"

                // Switch
                switchActivo.isChecked = programacion.activo
                switchActivo.setOnCheckedChangeListener { _, isChecked ->
                    onSwitchChanged(programacion, isChecked)
                }

                // Botones
                btnEditar.setOnClickListener {
                    onEditClick(programacion)
                }

                btnEliminar.setOnClickListener {
                    onDeleteClick(programacion)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramacionViewHolder {
        val binding = ItemProgramacionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProgramacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgramacionViewHolder, position: Int) {
        holder.bind(programaciones[position])
    }

    override fun getItemCount(): Int = programaciones.size
}