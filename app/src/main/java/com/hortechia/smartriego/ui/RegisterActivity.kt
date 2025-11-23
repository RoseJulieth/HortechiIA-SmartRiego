package com.hortechia.smartriego.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.hortechia.smartriego.R
import com.hortechia.smartriego.utils.Validators

/**
 * RegisterActivity - Pantalla de registro de nuevos usuarios
 *
 * Funcionalidad:
 * - Validación de datos según OWASP
 * - Creación de usuario en Firebase Auth
 * - Guardado de perfil en Realtime Database
 * - Navegación automática al Dashboard
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Inicializar vistas
        initViews()

        // Configurar listeners
        setupListeners()
    }

    /**
     * Inicializa las referencias a las vistas
     */
    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tilName = findViewById(R.id.tilName)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    /**
     * Configura los listeners
     */
    private fun setupListeners() {
        // Botón atrás
        btnBack.setOnClickListener {
            finish()
        }

        // Botón registrar
        btnRegister.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }

        // Link "Inicia sesión"
        tvLogin.setOnClickListener {
            finish()
        }

        // Limpiar errores al enfocar
        etName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilName.error = null
        }

        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilEmail.error = null
        }

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilPassword.error = null
        }

        etConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilConfirmPassword.error = null
        }
    }

    /**
     * Valida todos los campos de entrada
     */
    private fun validateInputs(): Boolean {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        var isValid = true

        // Validar nombre
        if (!Validators.isNotEmpty(name)) {
            tilName.error = getString(R.string.error_empty_field)
            isValid = false
        } else {
            tilName.error = null
        }

        // Validar email
        if (!Validators.isNotEmpty(email)) {
            tilEmail.error = getString(R.string.error_empty_field)
            isValid = false
        } else if (!Validators.isValidEmail(email)) {
            tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            tilEmail.error = null
        }

        // Validar contraseña
        if (!Validators.isNotEmpty(password)) {
            tilPassword.error = getString(R.string.error_empty_field)
            isValid = false
        } else if (!Validators.isValidPasswordLength(password)) {
            tilPassword.error = getString(R.string.error_short_password)
            isValid = false
        } else if (!Validators.isStrongPassword(password)) {
            tilPassword.error = getString(R.string.error_weak_password)
            isValid = false
        } else {
            tilPassword.error = null
        }

        // Validar confirmación de contraseña
        if (!Validators.isNotEmpty(confirmPassword)) {
            tilConfirmPassword.error = getString(R.string.error_empty_field)
            isValid = false
        } else if (!Validators.passwordsMatch(password, confirmPassword)) {
            tilConfirmPassword.error = getString(R.string.error_passwords_dont_match)
            isValid = false
        } else {
            tilConfirmPassword.error = null
        }

        return isValid
    }

    /**
     * Registra el usuario en Firebase
     */
    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        showLoading(true)

        // Crear usuario en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Usuario creado exitosamente
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        // Guardar datos del usuario en Realtime Database
                        saveUserToDatabase(userId, name, email)
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Error al obtener ID de usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showLoading(false)
                    handleRegisterError(task.exception)
                }
            }
    }

    /**
     * Guarda los datos del usuario en Firebase Realtime Database
     */
    private fun saveUserToDatabase(userId: String, name: String, email: String) {
        val userRef = database.reference.child("users").child(userId)

        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "createdAt" to System.currentTimeMillis(),
            "role" to "user"
        )

        userRef.setValue(userData)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    getString(R.string.success_register),
                    Toast.LENGTH_SHORT
                ).show()

                // Navegar a MainActivity (Dashboard en el futuro)
                val intent = Intent(this, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al guardar datos: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Maneja errores de registro
     */
    private fun handleRegisterError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("already in use") == true ->
                getString(R.string.error_email_already_exists)
            exception?.message?.contains("badly formatted") == true ->
                getString(R.string.error_invalid_email)
            exception?.message?.contains("weak password") == true ->
                getString(R.string.error_weak_password)
            else -> "${getString(R.string.error_register_failed)}: ${exception?.message}"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    /**
     * Muestra u oculta el loading
     */
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !show
        etName.isEnabled = !show
        etEmail.isEnabled = !show
        etPassword.isEnabled = !show
        etConfirmPassword.isEnabled = !show
    }
}