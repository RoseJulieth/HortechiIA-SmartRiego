package com.hortechia.smartriego.utils

import android.util.Patterns

/**
 * Utilidades para validación de datos de usuario
 * Implementa validaciones según estándares OWASP
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
object Validators {

    /**
     * Valida que un campo no esté vacío
     */
    fun isNotEmpty(text: String): Boolean {
        return text.trim().isNotEmpty()
    }

    /**
     * Valida formato de email según estándar RFC 5322
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Valida longitud mínima de contraseña
     * OWASP recomienda mínimo 8 caracteres, usamos 6 para UX
     */
    fun isValidPasswordLength(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Valida que la contraseña contenga:
     * - Al menos una mayúscula
     * - Al menos una minúscula
     * - Al menos un número
     *
     * Basado en OWASP Authentication Cheat Sheet
     */
    fun isStrongPassword(password: String): Boolean {
        if (password.length < 6) return false

        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }

        return hasUpperCase && hasLowerCase && hasDigit
    }

    /**
     * Valida que dos contraseñas coincidan
     */
    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    /**
     * Valida nombre completo (al menos 2 palabras)
     */
    fun isValidName(name: String): Boolean {
        val trimmedName = name.trim()
        return trimmedName.isNotEmpty() && trimmedName.split(" ").size >= 2
    }
}