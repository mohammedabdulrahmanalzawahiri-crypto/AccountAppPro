package com.abdelrahman.accountpromax.ui.projects

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdelrahman.accountpromax.databinding.FragmentProjectsBinding
import com.abdelrahman.accountpromax.ui.main.MainViewModel
import com.abdelrahman.accountpromax.utils.UiStyleManager

class ProjectsFragment : Fragment() {
    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!
    private val vm by activityViewModels<MainViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        binding.projectsRecycler.layoutManager = LinearLayoutManager(requireContext())
        vm.projects.observe(viewLifecycleOwner) { projects ->
            binding.projectsRecycler.adapter = ProjectsAdapter(
                items = projects,
                selectedId = vm.selectedProjectId.value ?: -1L,
                typeface = UiStyleManager.typeface(requireContext()),
                onClick = {
                    vm.selectProject(it.id)
                },
                onLongClick = { project ->
                    showProjectActions(project.id, project.name)
                }
            )
        }

        binding.addProjectBtn.setOnClickListener {
            val input = EditText(requireContext())
            AlertDialog.Builder(requireContext())
                .setTitle("إضافة مهمة")
                .setView(input)
                .setPositiveButton("حفظ") { _, _ ->
                    val name = input.text?.toString().orEmpty().trim()
                    if (name.isNotEmpty()) vm.addProject(name)
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
    }

    private fun showProjectActions(id: Long, name: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(name)
            .setItems(arrayOf("تعديل الاسم", "حذف الدفتر")) { _, which ->
                if (which == 0) {
                    val input = EditText(requireContext()).apply { setText(name) }
                    AlertDialog.Builder(requireContext())
                        .setTitle("تعديل الدفتر")
                        .setView(input)
                        .setPositiveButton("حفظ") { _, _ ->
                            val newName = input.text?.toString().orEmpty().trim()
                            if (newName.isNotEmpty()) vm.renameProject(id, newName)
                        }
                        .setNegativeButton("إلغاء", null)
                        .show()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("تأكيد الحذف")
                        .setMessage("لا يمكن التراجع بعد الحذف.")
                        .setPositiveButton("حذف") { _, _ -> vm.deleteProject(id) }
                        .setNegativeButton("إلغاء", null)
                        .show()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
