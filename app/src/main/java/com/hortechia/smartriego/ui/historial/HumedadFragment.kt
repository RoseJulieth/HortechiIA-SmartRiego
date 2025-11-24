package com.hortechia.smartriego.ui.historial

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.hortechia.smartriego.databinding.FragmentHistorialHumedadBinding

class HumedadFragment : Fragment() {

    private var _binding: FragmentHistorialHumedadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialHumedadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        loadChartData()
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            // Configurar eje X
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    private val days = arrayOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                    override fun getFormattedValue(value: Float): String {
                        return days.getOrNull(value.toInt()) ?: ""
                    }
                }
            }

            // Configurar eje Y izquierdo
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 100f
                enableGridDashedLine(10f, 10f, 0f)
            }

            // Desactivar eje Y derecho
            axisRight.isEnabled = false

            // Leyenda
            legend.isEnabled = false

            // Animación
            animateX(1000)
        }
    }

    private fun loadChartData() {
        // Datos de ejemplo - Zona 1 (Tomates)
        val zona1Entries = listOf(
            Entry(0f, 72f),
            Entry(1f, 68f),
            Entry(2f, 45f),
            Entry(3f, 70f),
            Entry(4f, 65f),
            Entry(5f, 48f),
            Entry(6f, 70f)
        )

        // Datos de ejemplo - Zona 2 (Césped)
        val zona2Entries = listOf(
            Entry(0f, 45f),
            Entry(1f, 50f),
            Entry(2f, 55f),
            Entry(3f, 48f),
            Entry(4f, 52f),
            Entry(5f, 35f),
            Entry(6f, 50f)
        )

        // Dataset Zona 1
        val zona1DataSet = LineDataSet(zona1Entries, "Zona 1 - Tomates").apply {
            color = Color.parseColor("#FF9800")
            setCircleColor(Color.parseColor("#FF9800"))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 9f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // Dataset Zona 2
        val zona2DataSet = LineDataSet(zona2Entries, "Zona 2 - Césped").apply {
            color = Color.parseColor("#4CAF50")
            setCircleColor(Color.parseColor("#4CAF50"))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 9f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        // Líneas de umbrales
        val umbralMinimoEntries = listOf(
            Entry(0f, 30f),
            Entry(6f, 30f)
        )
        val umbralMinimoDataSet = LineDataSet(umbralMinimoEntries, "Umbral Mínimo").apply {
            color = Color.parseColor("#FF5252")
            lineWidth = 1.5f
            setDrawCircles(false)
            setDrawValues(false)
            enableDashedLine(10f, 5f, 0f)
        }

        val umbralOptimoEntries = listOf(
            Entry(0f, 70f),
            Entry(6f, 70f)
        )
        val umbralOptimoDataSet = LineDataSet(umbralOptimoEntries, "Umbral Óptimo").apply {
            color = Color.parseColor("#66BB6A")
            lineWidth = 1.5f
            setDrawCircles(false)
            setDrawValues(false)
            enableDashedLine(10f, 5f, 0f)
        }

        // Agregar todos los datasets
        val lineData = LineData(zona1DataSet, zona2DataSet, umbralMinimoDataSet, umbralOptimoDataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}