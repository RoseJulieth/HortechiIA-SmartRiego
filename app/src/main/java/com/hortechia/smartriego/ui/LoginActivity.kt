package com.hortechia.smartriego.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.hortechia.smartriego.ui.DashboardActivity
import com.hortechia.smartriego.R
import com.hortechia.smartriego.utils.Validators

/**
 * LoginActivity - Pantalla de inicio de sesión
 *
 * Funcionalidad:
 * - Validación de email y contraseña
 * - Autenticación con Firebase Auth
 * - Login con Google
 * - Navegación a Dashboard si login exitoso
 * - Links a Registro y Recuperación de contraseña
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btnGoogleSignIn: SignInButton

    // Launcher para el resultado de Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        } else {
            Toast.makeText(this, getString(R.string.google_sign_in_cancelled), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configurar Google Sign-In
        configureGoogleSignIn()

        // Inicializar vistas
        initViews()

        // Configurar listeners
        setupListeners()
    }

    /**
     * Inicializa las referencias a las vistas
     */
    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvRegister = findViewById(R.id.tvRegister)
        progressBar = findViewById(R.id.progressBar)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
    }

    /**
     * Configura los listeners de los botones y campos
     */
    private fun setupListeners() {
        // Click en botón Login
        btnLogin.setOnClickListener {
            if (validateInputs()) {
                loginUser()
            }
        }

        // Click en botón Google Sign-In
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // Click en "Regístrate"
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Click en "¿Olvidaste tu contraseña?"
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Limpiar error al escribir
        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilEmail.error = null
        }

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilPassword.error = null
        }
    }

    /**
     * Valida los campos de entrada
     * @return true si todos los campos son válidos
     */
    private fun validateInputs(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        var isValid = true

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
        } else {
            tilPassword.error = null
        }

        return isValid
    }

    /**
     * Realiza el login con Firebase Authentication
     */
    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        // Mostrar loading
        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    // Login exitoso
                    Toast.makeText(
                        this,
                        getString(R.string.success_login),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Guardar que ya inició sesión
                    saveLoginState(true)

                    // Navegar a Dashboard (por ahora MainActivity)
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Error en login
                    handleLoginError(task.exception)
                }
            }
    }

    /**
     * Maneja los errores de autenticación
     */
    private fun handleLoginError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("no user record") == true ->
                getString(R.string.error_user_not_found)
            exception?.message?.contains("password is invalid") == true ->
                getString(R.string.error_wrong_password)
            else -> getString(R.string.error_login_failed)
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    /**
     * Muestra u oculta el indicador de carga
     */
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnGoogleSignIn.isEnabled = !show
        etEmail.isEnabled = !show
        etPassword.isEnabled = !show
    }

    /**
     * Guarda el estado de sesión en SharedPreferences
     */
    private fun saveLoginState(isLoggedIn: Boolean) {
        val sharedPref = getSharedPreferences("HortechIA", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }

    /**
     * Configura Google Sign-In
     */
    private fun configureGoogleSignIn() {
        // Configurar opciones de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    /**
     * Inicia el flujo de Google Sign-In
     */
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    /**
     * Maneja el resultado de Google Sign-In
     */
    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!)
            }
        } catch (e: ApiException) {
            Toast.makeText(
                this,
                "${getString(R.string.google_sign_in_error)}: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Autentica con Firebase usando el token de Google
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        showLoading(true)

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    // Login exitoso
                    val user = auth.currentUser

                    // Guardar usuario en Database si es nuevo
                    if (user != null) {
                        saveGoogleUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "")
                    }

                    Toast.makeText(
                        this,
                        getString(R.string.success_login),
                        Toast.LENGTH_SHORT
                    ).show()

                    saveLoginState(true)

                    // Navegar a Dashboard
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "${getString(R.string.error_login_failed)}: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /**
     * Guarda el usuario de Google en Realtime Database
     */
    private fun saveGoogleUserToDatabase(userId: String, name: String, email: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.reference.child("users").child(userId)

        // Verificar si el usuario ya existe
        userRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                // Usuario nuevo, guardarlo
                val userData = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis(),
                    "role" to "user",
                    "loginMethod" to "google"
                )

                userRef.setValue(userData)
            }
        }
    }
}