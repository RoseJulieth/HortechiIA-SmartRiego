package com.hortechia.smartriego

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hortechia.smartriego.adapter.ProgramacionAdapter
import com.hortechia.smartriego.databinding.ActivityProgramacionBinding
import com.hortechia.smartriego.models.ProgramacionData
import com.hortechia.smartriego.ui.ControlManualActivity
import com.hortechia.smartriego.ui.DashboardActivity
import com.hortechia.smartriego.HistorialActivity // Agrega esta importación si falta
import com.hortechia.smartriego.ConfiguracionActivity // Agrega esta importación si falta

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
        // FAB Agregar (Ahora funcional)
        binding.fabAgregar.setOnClickListener {
            mostrarDialogAgregar()
        }

        // Switch Modo Inteligente (Ahora da feedback visual)
        binding.switchModoInteligente.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "🧠 Modo Inteligente ACTIVADO: Se suspenderán riegos por lluvia", Toast.LENGTH_LONG).show()
                binding.toolbar.subtitle = "Modo IA: Activo" // Indicador visual en toolbar
            } else {
                Toast.makeText(this, "Modo Inteligente desactivado", Toast.LENGTH_SHORT).show()
                binding.toolbar.subtitle = null
            }
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
                R.id.nav_schedule -> true
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
        val estado = if (isChecked) "Activado" else "Desactivado"
        Toast.makeText(this, "${programacion.nombre}: $estado", Toast.LENGTH_SHORT).show()
    }

    // --- AQUÍ ESTÁ LA MAGIA DE EDICIÓN ---
    private fun onEditarProgramacion(programacion: ProgramacionData) {
        // Creamos un layout simple programáticamente o inflamos uno si prefieres
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar: ${programacion.nombre}")

        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre del programa"
        inputNombre.setText(programacion.nombre)
        layout.addView(inputNombre)

        val inputDuracion = EditText(this)
        inputDuracion.hint = "Duración (minutos)"
        inputDuracion.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        inputDuracion.setText(programacion.duracion.toString())
        layout.addView(inputDuracion)

        // TimePicker (Reloj)
        val timeLabel = android.widget.TextView(this)
        timeLabel.text = "Hora de inicio:"
        timeLabel.setPadding(0, 20, 0, 10)
        layout.addView(timeLabel)

        val timePicker = TimePicker(this)
        timePicker.setIs24HourView(true)
        // Modo spinner para que no ocupe toda la pantalla
        timePicker.hour = 18
        timePicker.minute = 0
        layout.addView(timePicker)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nuevoNombre = inputNombre.text.toString()
            val nuevaDuracion = inputDuracion.text.toString().toIntOrNull() ?: 10
            val hora = "${timePicker.hour}:${String.format("%02d", timePicker.minute)}"

            // Actualizamos el objeto en la lista (Simulando persistencia)
            val index = programaciones.indexOf(programacion)
            if (index != -1) {
                programaciones[index] = programacion.copy(
                    nombre = nuevoNombre,
                    duracion = nuevaDuracion,
                    hora = "Todos los días, $hora",
                    proximaEjecucion = "Hoy $hora"
                )
                adapter.notifyItemChanged(index)
                Toast.makeText(this, "Programación actualizada", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun onEliminarProgramacion(programacion: ProgramacionData) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar programación")
            .setMessage("¿Estás seguro de eliminar '${programacion.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                programaciones.remove(programacion)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Programación eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- MAGIA DE AGREGAR ---
    private fun mostrarDialogAgregar() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nueva Programación")

        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre (ej. Riego Rosas)"
        layout.addView(inputNombre)

        val inputDuracion = EditText(this)
        inputDuracion.hint = "Duración (minutos)"
        inputDuracion.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(inputDuracion)

        builder.setView(layout)

        builder.setPositiveButton("Crear") { _, _ ->
            val nombre = inputNombre.text.toString()
            val duracion = inputDuracion.text.toString().toIntOrNull() ?: 15

            if (nombre.isNotEmpty()) {
                val nuevaProg = ProgramacionData(
                    id = "prog_${System.currentTimeMillis()}",
                    nombre = nombre,
                    zona = "Zona 1", // Por defecto
                    dias = listOf("L", "M", "X", "J", "V"),
                    hora = "Lunes a Viernes, 07:00 AM",
                    duracion = duracion,
                    activo = true,
                    proximaEjecucion = "Mañana 07:00 AM"
                )
                programaciones.add(nuevaProg)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Programación creada con éxito", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}