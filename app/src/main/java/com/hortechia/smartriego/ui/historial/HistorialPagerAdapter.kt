package com.hortechia.smartriego

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hortechia.smartriego.ui.historial.HumedadFragment
import com.hortechia.smartriego.ui.historial.RiegosFragment
import com.hortechia.smartriego.ui.historial.ConsumoFragment

class HistorialPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HumedadFragment()
            1 -> RiegosFragment()
            2 -> ConsumoFragment()
            else -> HumedadFragment()
        }
    }
}