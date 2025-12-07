package com.hortechia.smartriego

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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
import com.hortechia.smartriego.HistorialActivity
import com.hortechia.smartriego.ConfiguracionActivity

class ProgramacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgramacionBinding
    private lateinit var adapter: ProgramacionAdapter
    private val programaciones = mutableListOf<ProgramacionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inflar el diseño que me enviaste
        binding = ActivityProgramacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Configurar componentes
        setupToolbar()
        setupRecyclerView()
        loadProgramaciones()
        setupListeners()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        // La flecha atrás solo cierra esta pantalla para volver al Dashboard
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProgramacionAdapter(
            programaciones = programaciones,
            onSwitchChanged = { programacion, isChecked ->
                // Guardar estado (Simulado)
                val index = programaciones.indexOf(programacion)
                if (index != -1) {
                    programaciones[index] = programacion.copy(activo = isChecked)
                }
                val estado = if (isChecked) "Activado" else "Desactivado"
                Toast.makeText(this, "${programacion.nombre}: $estado", Toast.LENGTH_SHORT).show()
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
        // Datos Mock para la demo
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
        // Tu FAB flotante
        binding.fabAgregar.setOnClickListener {
            mostrarDialogAgregar()
        }

        // Tu Switch Inteligente
        binding.switchModoInteligente.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "🧠 Modo Inteligente ACTIVADO", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Modo Inteligente desactivado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_schedule

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // CORRECCIÓN CRÍTICA: finish() vuelve al Dashboard sin crear uno nuevo
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
                R.id.nav_schedule -> true // Ya estamos aquí
                R.id.nav_settings -> {
                    startActivity(Intent(this, ConfiguracionActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // --- LÓGICA DE DIÁLOGOS (Construidos aquí para evitar errores XML) ---

    private fun mostrarDialogAgregar() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nueva Programación")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre (ej. Riego Rosas)"
        layout.addView(inputNombre)

        val inputDuracion = EditText(this)
        inputDuracion.hint = "Duración (minutos)"
        inputDuracion.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(inputDuracion)

        builder.setView(layout)

        builder.setPositiveButton("Crear") { _, _ ->
            val nombre = inputNombre.text.toString()
            val duracion = inputDuracion.text.toString().toIntOrNull() ?: 15

            if (nombre.isNotEmpty()) {
                val nuevaProg = ProgramacionData(
                    id = "prog_${System.currentTimeMillis()}",
                    nombre = nombre,
                    zona = "Zona 1",
                    dias = listOf("L", "M", "X", "J", "V"),
                    hora = "Lunes a Viernes, 07:00 AM",
                    duracion = duracion,
                    activo = true,
                    proximaEjecucion = "Mañana 07:00 AM"
                )
                programaciones.add(nuevaProg)
                adapter.notifyItemInserted(programaciones.size - 1)
                Toast.makeText(this, "Programación creada", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun onEditarProgramacion(programacion: ProgramacionData) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar: ${programacion.nombre}")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre"
        inputNombre.setText(programacion.nombre)
        layout.addView(inputNombre)

        val inputDuracion = EditText(this)
        inputDuracion.hint = "Duración (min)"
        inputDuracion.inputType = InputType.TYPE_CLASS_NUMBER
        inputDuracion.setText(programacion.duracion.toString())
        layout.addView(inputDuracion)

        val timePicker = TimePicker(this)
        timePicker.setIs24HourView(true)
        layout.addView(timePicker)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nuevoNombre = inputNombre.text.toString()
            val nuevaDuracion = inputDuracion.text.toString().toIntOrNull() ?: 10
            val horaStr = "${timePicker.hour}:${String.format("%02d", timePicker.minute)}"

            val index = programaciones.indexOf(programacion)
            if (index != -1) {
                programaciones[index] = programacion.copy(
                    nombre = nuevoNombre,
                    duracion = nuevaDuracion,
                    hora = "Todos los días, $horaStr",
                    proximaEjecucion = "Hoy $horaStr"
                )
                adapter.notifyItemChanged(index)
                Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun onEliminarProgramacion(programacion: ProgramacionData) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Estás seguro de eliminar '${programacion.nombre}'?")
            .setPositiveButton("Sí") { _, _ ->
                val index = programaciones.indexOf(programacion)
                if (index != -1) {
                    programaciones.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}