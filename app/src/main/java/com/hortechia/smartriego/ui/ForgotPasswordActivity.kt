package com.hortechia.smartriego.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.hortechia.smartriego.R
import com.hortechia.smartriego.utils.Validators

/**
 * ForgotPasswordActivity - Recuperación de contraseña
 */
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSend: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Por ahora usamos el layout de login (puedes crear uno específico después)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // TODO: Crear layout específico para forgot password
        Toast.makeText(this, "Función en desarrollo", Toast.LENGTH_SHORT).show()
        finish()
    }
}