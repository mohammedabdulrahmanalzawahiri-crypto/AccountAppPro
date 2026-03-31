package com.abdelrahman.accountpromax.ui.charts

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.abdelrahman.accountpromax.databinding.FragmentChartsBinding
import com.abdelrahman.accountpromax.ui.main.MainViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class ChartsFragment : Fragment() {
    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!
    private val vm by activityViewModels<MainViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        vm.balances(vm.selectedProjectId.value ?: 1L).observe(viewLifecycleOwner) { balances ->
            val totalLeh = balances.sumOf { it.lehTotal }.toFloat()
            val totalAleh = balances.sumOf { it.alehTotal }.toFloat()

            binding.pieChart.data = PieData(
                PieDataSet(
                    listOf(PieEntry(totalLeh, "له"), PieEntry(totalAleh, "عليه")),
                    "توزيع الديون"
                ).apply { colors = listOf(Color.GREEN, Color.RED) }
            )
            binding.pieChart.invalidate()

            val barEntries = balances.take(10).mapIndexed { idx, item -> BarEntry(idx.toFloat(), item.balance.toFloat()) }
            binding.barChart.data = BarData(BarDataSet(barEntries, "أكبر العملاء"))
            binding.barChart.invalidate()

            val lineEntries = balances.mapIndexed { idx, item -> Entry(idx.toFloat(), item.balance.toFloat()) }
            binding.lineChart.data = LineData(LineDataSet(lineEntries, "حركة الحساب"))
            binding.lineChart.invalidate()
        }
    }
}
