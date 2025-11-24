package com.hortechia.smartriego.ui.historial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hortechia.smartriego.databinding.FragmentHistorialRiegosBinding

class RiegosFragment : Fragment() {

    private var _binding: FragmentHistorialRiegosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialRiegosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Por ahora solo mostramos el empty state
        binding.emptyStateRiegos.visibility = View.VISIBLE
        binding.recyclerViewRiegos.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}