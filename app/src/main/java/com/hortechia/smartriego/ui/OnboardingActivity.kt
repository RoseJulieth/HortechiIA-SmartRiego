package com.hortechia.smartriego.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.hortechia.smartriego.R
import com.hortechia.smartriego.adapter.OnboardingAdapter
import com.hortechia.smartriego.model.OnboardingSlide

/**
 * OnboardingActivity - Pantalla de bienvenida con tutorial de 3 slides
 *
 * Funcionalidad:
 * - Muestra 3 slides educativos sobre HortechIA
 * - Navegación por swipe o botones
 * - Indicadores de página actualizados
 * - Botón "Saltar" (solo en slides 1-2)
 * - Botón "Siguiente" / "Comenzar"
 * - Guarda que el usuario ya vio el onboarding
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 * @version 1.0
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorsContainer: LinearLayout
    private lateinit var btnNext: Button
    private lateinit var tvSkip: TextView

    private val slides = listOf(
        OnboardingSlide(
            R.drawable.onboarding_slide1,
            R.string.onboarding_slide1_title,
            R.string.onboarding_slide1_desc
        ),
        OnboardingSlide(
            R.drawable.onboarding_slide2,
            R.string.onboarding_slide2_title,
            R.string.onboarding_slide2_desc
        ),
        OnboardingSlide(
            R.drawable.onboarding_slide3,
            R.string.onboarding_slide3_title,
            R.string.onboarding_slide3_desc
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Inicializar vistas
        viewPager = findViewById(R.id.viewPager)
        indicatorsContainer = findViewById(R.id.indicatorsContainer)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)

        // Configurar adapter del ViewPager
        viewPager.adapter = OnboardingAdapter(slides)

        // Crear indicadores de página
        setupIndicators()
        setCurrentIndicator(0)

        // Listener para cambios de página
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

                // Actualizar texto del botón y visibilidad del "Saltar"
                if (position == slides.size - 1) {
                    // Último slide
                    btnNext.text = getString(R.string.onboarding_start)
                    tvSkip.visibility = View.GONE
                } else {
                    // Slides 1 y 2
                    btnNext.text = getString(R.string.onboarding_next)
                    tvSkip.visibility = View.VISIBLE
                }
            }
        })

        // Click en botón Siguiente/Comenzar
        btnNext.setOnClickListener {
            if (viewPager.currentItem < slides.size - 1) {
                // Ir al siguiente slide
                viewPager.currentItem += 1
            } else {
                // Último slide - ir a Login
                finishOnboarding()
            }
        }

        // Click en Saltar
        tvSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    /**
     * Crea los indicadores de página (dots) dinámicamente
     */
    private fun setupIndicators() {
        val indicators = Array(slides.size) { ImageView(this) }
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        indicators.forEach {
            it.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.indicator_inactive
                )
            )
            it.layoutParams = layoutParams
            indicatorsContainer.addView(it)
        }
    }

    /**
     * Actualiza el indicador activo según la página actual
     */
    private fun setCurrentIndicator(position: Int) {
        val childCount = indicatorsContainer.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorsContainer.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

    /**
     * Marca el onboarding como completado y navega al Login
     */
    private fun finishOnboarding() {
        // Guardar en SharedPreferences que ya vio el onboarding
        val sharedPref = getSharedPreferences("HortechIA", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("onboarding_completed", true).apply()

        // Navegar al Login
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}