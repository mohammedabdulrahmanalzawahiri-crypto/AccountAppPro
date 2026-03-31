package com.abdelrahman.accountpromax.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdelrahman.accountpromax.R
import com.abdelrahman.accountpromax.adapters.ClientAdapter
import com.abdelrahman.accountpromax.databinding.FragmentHomeBinding
import com.abdelrahman.accountpromax.ui.dialog.AddTransactionBottomSheet
import com.abdelrahman.accountpromax.ui.main.MainViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vm by activityViewModels<MainViewModel>()
    private lateinit var adapter: ClientAdapter
    private val debounce = Handler(Looper.getMainLooper())
    private var sourceList = emptyList<com.abdelrahman.accountpromax.models.ClientBalanceUi>()
    private var activeBalances: LiveData<List<com.abdelrahman.accountpromax.models.ClientBalanceUi>>? = null
    private val balancesObserver = Observer<List<com.abdelrahman.accountpromax.models.ClientBalanceUi>> { list ->
        sourceList = list
        adapter.submitList(list)
        binding.totalLeh.text = "إجمالي له: %.2f".format(list.sumOf { it.lehTotal })
        binding.totalAleh.text = "إجمالي عليه: %.2f".format(list.sumOf { it.alehTotal })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        adapter = ClientAdapter(
            onClick = {
                vm.selectedClientId.value = it.clientId
                vm.selectedClientName.value = it.clientName
                findNavController().navigate(R.id.clientDetailsFragment)
            },
            onLongClick = { client ->
                showClientActions(client.clientId, client.clientName)
            }
        )
        binding.clientsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.clientsRecycler.adapter = adapter

        vm.selectedProjectId.observe(viewLifecycleOwner) { projectId ->
            activeBalances?.removeObserver(balancesObserver)
            activeBalances = vm.balances(projectId).also { it.observe(viewLifecycleOwner, balancesObserver) }
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                debounce.removeCallbacksAndMessages(null)
                debounce.postDelayed({
                    val term = newText.orEmpty().trim()
                    val filtered = sourceList.filter { it.clientName.contains(term, true) }
                    adapter.submitList(filtered)
                }, 250)
                return true
            }
        })

        binding.addFab.setOnClickListener {
            AddTransactionBottomSheet().show(parentFragmentManager, "add_tx")
        }
    }

    private fun showClientActions(clientId: Long, currentName: String) {
        val options = arrayOf("تعديل الاسم", "حذف العميل")
        AlertDialog.Builder(requireContext())
            .setTitle(currentName)
            .setItems(options) { _, which ->
                if (which == 0) {
                    val input = EditText(requireContext()).apply { setText(currentName) }
                    AlertDialog.Builder(requireContext())
                        .setTitle("تعديل اسم العميل")
                        .setView(input)
                        .setPositiveButton("حفظ") { _, _ ->
                            val newName = input.text?.toString().orEmpty().trim()
                            if (newName.isNotEmpty()) vm.renameClient(clientId, newName)
                        }
                        .setNegativeButton("إلغاء", null)
                        .show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("تأكيد الحذف")
                        .setMessage("هل تريد حذف العميل وكل عملياته؟")
                        .setPositiveButton("حذف") { _, _ -> vm.deleteClient(clientId) }
                        .setNegativeButton("إلغاء", null)
                        .show()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activeBalances?.removeObserver(balancesObserver)
        _binding = null
    }
}
