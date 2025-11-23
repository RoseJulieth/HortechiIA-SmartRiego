package com.hortechia.smartriego.ui

import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.hortechia.smartriego.R

/**
 * SplashActivity - Pantalla de inicio con logo HortechIA
 */
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION = 2500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            navegarASiguientePantalla()
        }, SPLASH_DURATION)
    }

    /**
     * Navega a la siguiente pantalla según el estado del usuario
     *
     * Lógica:
     * 1. Si hay sesión activa (Firebase) → DashboardActivity (MainActivity por ahora)
     * 2. Si NO hay sesión pero ya vio onboarding → LoginActivity
     * 3. Si es primera vez → OnboardingActivity
     */
    private fun navegarASiguientePantalla() {
        val sharedPref = getSharedPreferences("HortechIA", Context.MODE_PRIVATE)
        val onboardingCompleted = sharedPref.getBoolean("onboarding_completed", false)

        // Verificar si hay sesión activa en Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser

        val intent = when {
            // Caso 1: Usuario ya autenticado → Dashboard
            currentUser != null -> {
                Intent(this, DashboardActivity::class.java)
            }
            // Caso 2: No autenticado pero ya vio onboarding → Login
            onboardingCompleted -> {
                Intent(this, LoginActivity::class.java)
            }
            // Caso 3: Primera vez → Onboarding
            else -> {
                Intent(this, OnboardingActivity::class.java)
            }
        }

        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // No hacer nada
    }
}