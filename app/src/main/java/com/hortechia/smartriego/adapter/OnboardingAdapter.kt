package com.hortechia.smartriego.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hortechia.smartriego.R
import com.hortechia.smartriego.model.OnboardingSlide

/**
 * Adapter para el ViewPager2 del Onboarding
 * Maneja la lista de slides y los muestra uno a uno
 *
 * @param slides Lista de slides a mostrar
 *
 * @author Jennifer Astudillo & Carlos Velásquez
 */
class OnboardingAdapter(
    private val slides: List<OnboardingSlide>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    /**
     * ViewHolder que contiene las vistas de cada slide
     */
    inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.ivSlideImage)
        private val titleTextView: TextView = view.findViewById(R.id.tvSlideTitle)
        private val descriptionTextView: TextView = view.findViewById(R.id.tvSlideDescription)

        /**
         * Vincula los datos del slide con las vistas
         */
        fun bind(slide: OnboardingSlide) {
            imageView.setImageResource(slide.imageRes)
            titleTextView.setText(slide.titleRes)
            descriptionTextView.setText(slide.descriptionRes)
        }
    }

    /**
     * Crea una nueva vista para un slide
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_slide, parent, false)
        return OnboardingViewHolder(view)
    }

    /**
     * Vincula los datos de un slide específico con su vista
     */
    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(slides[position])
    }

    /**
     * Retorna la cantidad total de slides
     */
    override fun getItemCount(): Int = slides.size
}