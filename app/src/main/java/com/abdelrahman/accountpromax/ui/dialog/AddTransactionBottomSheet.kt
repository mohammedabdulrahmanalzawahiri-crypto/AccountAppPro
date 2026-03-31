package com.abdelrahman.accountpromax.ui.dialog

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import android.widget.EditText
import android.widget.ArrayAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.abdelrahman.accountpromax.databinding.DialogAddTransactionBinding
import com.abdelrahman.accountpromax.ui.main.MainViewModel
import com.abdelrahman.accountpromax.utils.CalculatorEvaluator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionBottomSheet : BottomSheetDialogFragment() {
    private var _binding: DialogAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val vm by activityViewModels<MainViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = DialogAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        binding.dateInput.setText(today)
        val projectId = vm.selectedProjectId.value ?: 1L
        vm.balances(projectId).observe(viewLifecycleOwner) { list ->
            val names = list.map { it.clientName }.distinct()
            binding.clientNameInput.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            )
        }

        binding.lehButton.setOnClickListener { save("leh") }
        binding.alehButton.setOnClickListener { save("aleh") }
        binding.amountInput.setOnClickListener { showCalculator() }
    }

    private fun showCalculator() {
        val input = EditText(requireContext()).apply {
            hint = "مثال: 10+5*2"
            setText(binding.amountInput.text?.toString().orEmpty())
        }
        AlertDialog.Builder(requireContext())
            .setTitle("آلة حاسبة")
            .setView(input)
            .setPositiveButton("احتساب") { _, _ ->
                val expr = input.text?.toString().orEmpty()
                val value = CalculatorEvaluator.eval(expr)
                if (value == null) {
                    Toast.makeText(requireContext(), "عملية غير صحيحة", Toast.LENGTH_SHORT).show()
                } else {
                    binding.amountInput.setText(value.toString())
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun save(type: String) {
        val name = binding.clientNameInput.text?.toString().orEmpty().trim()
        val amount = binding.amountInput.text?.toString().orEmpty().toDoubleOrNull()
        val desc = binding.descInput.text?.toString().orEmpty().trim()
        val date = binding.dateInput.text?.toString().orEmpty().trim()

        if (name.isEmpty() || amount == null || amount <= 0 || date.isEmpty()) {
            Toast.makeText(requireContext(), "تحقق من البيانات", Toast.LENGTH_SHORT).show()
            return
        }
        vm.addTransaction(name, amount, type, date, desc)
        val vib = requireContext().getSystemService(Vibrator::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vib?.vibrate(50)
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
