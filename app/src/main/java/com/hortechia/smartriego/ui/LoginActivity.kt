package com.hortechia.smartriego.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.hortechia.smartriego.R
import com.hortechia.smartriego.utils.Validators

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

        auth = FirebaseAuth.getInstance()
        configureGoogleSignIn()
        initViews()
        setupListeners()
    }

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

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            if (validateInputs()) {
                loginUser()
            }
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // --- CORRECCIÓN AQUÍ: Lógica de recuperación de contraseña ---
        tvForgotPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordReset(email)
            } else {
                showEmailDialogForReset()
            }
        }

        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilEmail.error = null
        }

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) tilPassword.error = null
        }
    }

    // --- NUEVAS FUNCIONES PARA RECUPERAR CONTRASEÑA ---
    private fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, "📧 Correo enviado a $email", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEmailDialogForReset() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.hint = "Ingresa tu correo"

        AlertDialog.Builder(this)
            .setTitle("Recuperar Contraseña")
            .setMessage("Ingresa tu correo registrado para enviarte un enlace.")
            .setView(input)
            .setPositiveButton("Enviar") { _, _ ->
                val mail = input.text.toString().trim()
                if (mail.isNotEmpty()) sendPasswordReset(mail)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    // ----------------------------------------------------

    private fun validateInputs(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        var isValid = true

        if (!Validators.isNotEmpty(email)) {
            tilEmail.error = getString(R.string.error_empty_field)
            isValid = false
        } else if (!Validators.isValidEmail(email)) {
            tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            tilEmail.error = null
        }

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

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.success_login), Toast.LENGTH_SHORT).show()
                    saveLoginState(true)
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun handleLoginError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("no user record") == true -> getString(R.string.error_user_not_found)
            exception?.message?.contains("password is invalid") == true -> getString(R.string.error_wrong_password)
            else -> getString(R.string.error_login_failed)
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnGoogleSignIn.isEnabled = !show
        etEmail.isEnabled = !show
        etPassword.isEnabled = !show
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        val sharedPref = getSharedPreferences("HortechIA", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account.idToken!!)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "${getString(R.string.google_sign_in_error)}: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        showLoading(true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveGoogleUserToDatabase(user.uid, user.displayName ?: "", user.email ?: "")
                    }
                    Toast.makeText(this, getString(R.string.success_login), Toast.LENGTH_SHORT).show()
                    saveLoginState(true)
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "${getString(R.string.error_login_failed)}: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveGoogleUserToDatabase(userId: String, name: String, email: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.reference.child("users").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
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