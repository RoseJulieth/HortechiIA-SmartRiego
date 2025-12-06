package com.hortechia.smartriego.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.hortechia.smartriego.R


class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnLogout: Button

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etName = findViewById(R.id.etProfileName)
        etEmail = findViewById(R.id.etProfileEmail)
        btnSave = findViewById(R.id.btnSaveProfile)
        btnLogout = findViewById(R.id.btnLogout)

        loadUserData()

        btnSave.setOnClickListener {
            saveUserData()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            // Redirigir al Login (ajusta la clase si se llama diferente)
            // val intent = Intent(this, LoginActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        etEmail.setText(user?.email)

        if (user != null) {
            database.reference.child("users").child(user.uid).get()
                .addOnSuccessListener { snapshot ->
                    val name = snapshot.child("name").getValue(String::class.java)
                    etName.setText(name)
                }
        }
    }

    private fun saveUserData() {
        val user = auth.currentUser
        val newName = etName.text.toString()

        if (user != null && newName.isNotEmpty()) {
            val updates = mapOf<String, Any>("name" to newName)

            database.reference.child("users").child(user.uid).updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    finish() // Volver al dashboard
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}