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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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

    // Referencias Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgramacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("programacion")
        } else {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        loadProgramacionesFirebase() // Carga real
        setupListeners()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
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

    // --- CORRECCIÓN: Carga de datos reales ---
    private fun loadProgramacionesFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                programaciones.clear()
                for (data in snapshot.children) {
                    val prog = data.getValue(ProgramacionData::class.java)
                    if (prog != null) {
                        programaciones.add(prog)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error al cargar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupListeners() {
        binding.fabAgregar.setOnClickListener {
            mostrarDialogAgregar()
        }

        binding.switchModoInteligente.setOnCheckedChangeListener { _, isChecked ->
            // Aquí podrías guardar esta preferencia en Firebase también si quisieras
            if (isChecked) {
                Toast.makeText(this, "🧠 Modo Inteligente ACTIVADO", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_schedule
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { finish(); true }
                R.id.nav_irrigation -> { startActivity(Intent(this, ControlManualActivity::class.java)); finish(); true }
                R.id.nav_history -> { startActivity(Intent(this, HistorialActivity::class.java)); finish(); true }
                R.id.nav_schedule -> true
                R.id.nav_settings -> { startActivity(Intent(this, ConfiguracionActivity::class.java)); finish(); true }
                else -> false
            }
        }
    }

    // --- ACCIONES EN LA NUBE ---

    private fun onProgramacionToggle(programacion: ProgramacionData, isChecked: Boolean) {
        // Actualizamos en Firebase
        database.child(programacion.id).child("activo").setValue(isChecked)
    }

    private fun onEditarProgramacion(programacion: ProgramacionData) {
        // Reutilizamos el diálogo pero con datos precargados
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
        // Setear hora actual del objeto si es posible (requiere parseo, simplificado aquí)
        layout.addView(timePicker)

        builder.setView(layout)

        builder.setPositiveButton("Actualizar") { _, _ ->
            val nuevoNombre = inputNombre.text.toString()
            val nuevaDuracion = inputDuracion.text.toString().toIntOrNull() ?: 10
            val horaStr = "${timePicker.hour}:${String.format("%02d", timePicker.minute)}"

            if (nuevoNombre.isNotEmpty()) {
                val progActualizada = programacion.copy(
                    nombre = nuevoNombre,
                    duracion = nuevaDuracion,
                    hora = "Todos los días, $horaStr",
                    proximaEjecucion = "Hoy $horaStr"
                )
                // Guardar cambios en Firebase
                database.child(programacion.id).setValue(progActualizada)
                    .addOnSuccessListener { Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show() }
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
                // Borrar de Firebase
                database.child(programacion.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

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
                val id = database.push().key ?: return@setPositiveButton

                val nuevaProg = ProgramacionData(
                    id = id,
                    nombre = nombre,
                    zona = "Zona 1",
                    dias = listOf("L", "M", "V"), // Por defecto
                    hora = "08:00 AM", // Por defecto
                    duracion = duracion,
                    activo = true,
                    proximaEjecucion = "Mañana 08:00 AM"
                )

                // Guardar en Firebase
                database.child(id).setValue(nuevaProg)
                    .addOnSuccessListener { Toast.makeText(this, "Creado", Toast.LENGTH_SHORT).show() }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}