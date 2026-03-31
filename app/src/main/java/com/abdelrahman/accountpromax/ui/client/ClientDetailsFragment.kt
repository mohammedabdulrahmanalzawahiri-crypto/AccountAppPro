package com.abdelrahman.accountpromax.ui.client

import android.os.Bundle
import android.widget.EditText
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdelrahman.accountpromax.adapters.TransactionAdapter
import com.abdelrahman.accountpromax.databinding.FragmentClientDetailsBinding
import com.abdelrahman.accountpromax.models.TransactionTimelineItem
import com.abdelrahman.accountpromax.ui.main.MainViewModel

class ClientDetailsFragment : Fragment() {
    private var _binding: FragmentClientDetailsBinding? = null
    private val binding get() = _binding!!
    private val vm by activityViewModels<MainViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentClientDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        val adapter = TransactionAdapter { tx -> showTxActions(tx) }
        binding.transactionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsRecycler.adapter = adapter
        binding.clientTitle.text = vm.selectedClientName.value ?: "العميل"

        val clientId = vm.selectedClientId.value ?: return
        vm.transactions(clientId).observe(viewLifecycleOwner) { txs ->
            var running = 0.0
            val timeline = txs.map { tx ->
                running += if (tx.type == "leh") tx.amount else -tx.amount
                TransactionTimelineItem(tx, running)
            }
            adapter.submitList(timeline)
        }
    }

    private fun showTxActions(tx: com.abdelrahman.accountpromax.models.TransactionEntity) {
        val options = arrayOf("تعديل العملية", "حذف العملية")
        AlertDialog.Builder(requireContext())
            .setItems(options) { _, which ->
                if (which == 0) showEditDialog(tx) else showDeleteDialog(tx)
            }
            .show()
    }

    private fun showDeleteDialog(tx: com.abdelrahman.accountpromax.models.TransactionEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("تأكيد الحذف")
            .setMessage("حذف العملية نهائيًا؟")
            .setPositiveButton("حذف") { _, _ -> vm.deleteTransaction(tx) }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun showEditDialog(tx: com.abdelrahman.accountpromax.models.TransactionEntity) {
        val input = EditText(requireContext()).apply { setText(tx.amount.toString()) }
        AlertDialog.Builder(requireContext())
            .setTitle("تعديل المبلغ")
            .setView(input)
            .setPositiveButton("حفظ") { _, _ ->
                val amount = input.text?.toString().orEmpty().toDoubleOrNull() ?: return@setPositiveButton
                vm.updateTransaction(tx.copy(amount = amount))
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
