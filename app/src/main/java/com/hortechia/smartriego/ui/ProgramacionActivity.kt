package com.hortechia.smartriego

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hortechia.smartriego.adapter.ProgramacionAdapter
import com.hortechia.smartriego.databinding.ActivityProgramacionBinding
import com.hortechia.smartriego.models.ProgramacionData
import com.hortechia.smartriego.ui.ControlManualActivity
import com.hortechia.smartriego.ui.DashboardActivity

class ProgramacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgramacionBinding
    private lateinit var adapter: ProgramacionAdapter
    private val programaciones = mutableListOf<ProgramacionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgramacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadProgramaciones()
        setupListeners()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProgramacionAdapter(
            programaciones = programaciones,
            onSwitchChanged = { programacion, isChecked ->
                onProgramacionToggle(programacion, isChecked)
            },
            onEditClick = { programacion ->
                onEditarProgramacion(programacion)
            },
            onDeleteClick = { programacion ->
                onEliminarProgramacion(programacion)
            }
        )

        binding.recyclerViewProgramaciones.apply {
            layoutManager = LinearLayoutManager(this@ProgramacionActivity)
            adapter = this@ProgramacionActivity.adapter
        }
    }

    private fun loadProgramaciones() {
        // Datos de ejemplo
        programaciones.clear()
        programaciones.addAll(
            listOf(
                ProgramacionData(
                    id = "prog1",
                    nombre = "Riego matutino tomates",
                    zona = "Zona 1",
                    dias = listOf("L", "M", "X", "J", "V"),
                    hora = "Lunes a Viernes, 6:00 AM",
                    duracion = 15,
                    activo = true,
                    proximaEjecucion = "Mañana 6:00 AM"
                ),
                ProgramacionData(
                    id = "prog2",
                    nombre = "Riego vespertino césped",
                    zona = "Zona 2",
                    dias = listOf("L", "M", "X", "J", "V", "S", "D"),
                    hora = "Todos los días, 18:00",
                    duracion = 10,
                    activo = true,
                    proximaEjecucion = "Hoy 18:00"
                )
            )
        )

        adapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        // FAB Agregar
        binding.fabAgregar.setOnClickListener {
            mostrarDialogAgregar()
        }

        // Switch Modo Inteligente
        binding.switchModoInteligente.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "Modo Inteligente activado" else "Modo Inteligente desactivado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_schedule

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_irrigation -> {
                    startActivity(Intent(this, ControlManualActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistorialActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_schedule -> {
                    // Ya estamos aquí
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun onProgramacionToggle(programacion: ProgramacionData, isChecked: Boolean) {
        Toast.makeText(
            this,
            "${programacion.nombre}: ${if (isChecked) "Activado" else "Desactivado"}",
            Toast.LENGTH_SHORT
        ).show()
        // TODO: Actualizar en Firebase
    }

    private fun onEditarProgramacion(programacion: ProgramacionData) {
        Toast.makeText(this, "Editar: ${programacion.nombre}", Toast.LENGTH_SHORT).show()
        // TODO: Abrir dialog de edición
    }

    private fun onEliminarProgramacion(programacion: ProgramacionData) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar programación")
            .setMessage("¿Estás seguro de eliminar '${programacion.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                programaciones.remove(programacion)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Programación eliminada", Toast.LENGTH_SHORT).show()
                // TODO: Eliminar de Firebase
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogAgregar() {
        Toast.makeText(this, "Dialog agregar programación - Próximamente", Toast.LENGTH_SHORT).show()
        // TODO: Implementar dialog completo con TimePicker, días, etc.
    }
}