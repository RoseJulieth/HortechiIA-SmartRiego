package com.hortechia.smartriego.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Modelo de datos para cada slide del Onboarding
 *
 * @param imageRes ID del recurso de imagen (drawable)
 * @param titleRes ID del recurso de título (string)
 * @param descriptionRes ID del recurso de descripción (string)
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
data class OnboardingSlide(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)