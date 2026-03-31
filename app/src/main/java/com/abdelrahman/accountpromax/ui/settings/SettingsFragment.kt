package com.abdelrahman.accountpromax.ui.settings

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.abdelrahman.accountpromax.databinding.FragmentSettingsBinding
import com.abdelrahman.accountpromax.ui.main.MainViewModel
import com.abdelrahman.accountpromax.utils.BackupManager
import com.abdelrahman.accountpromax.utils.UiStyleManager

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val vm by activityViewModels<MainViewModel>()
    private var pendingImportType: String = "json"

    private val importer = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        val rows = if (pendingImportType == "json") {
            BackupManager.importJson(requireContext(), uri)
        } else {
            BackupManager.importExcel(requireContext(), uri)
        }
        vm.importRows(rows)
        Toast.makeText(requireContext(), "تم استيراد ${rows.size} عملية", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        val prefs = securePrefs()
        val (themeIdx, fontIdx, sizeIdx) = UiStyleManager.current(requireContext())
        val themes = UiStyleManager.themeNames()
        val fonts = UiStyleManager.fontNames()
        val sizes = UiStyleManager.sizeNames()
        binding.themePicker.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, themes))
        binding.fontPicker.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, fonts))
        binding.fontSizePicker.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sizes))
        binding.themePicker.setText(themes[themeIdx], false)
        binding.fontPicker.setText(fonts[fontIdx], false)
        binding.fontSizePicker.setText(sizes[sizeIdx], false)
        binding.passwordEdit.setText("")
        binding.securityQuestionEdit.setText(prefs.getString("security_question", ""))
        binding.lockSwitch.isChecked = prefs.getBoolean("lock_enabled", false)

        binding.saveSettingsBtn.setOnClickListener {
            val pass = binding.passwordEdit.text?.toString().orEmpty()
            val encoded = Base64.encodeToString(pass.toByteArray(), Base64.NO_WRAP)
            prefs.edit()
                .putString("password_b64", encoded)
                .putString("security_question", binding.securityQuestionEdit.text?.toString().orEmpty())
                .putBoolean("lock_enabled", binding.lockSwitch.isChecked)
                .apply()
            val newTheme = themes.indexOf(binding.themePicker.text?.toString().orEmpty()).coerceAtLeast(0)
            val newFont = fonts.indexOf(binding.fontPicker.text?.toString().orEmpty()).coerceAtLeast(0)
            val newSize = sizes.indexOf(binding.fontSizePicker.text?.toString().orEmpty()).coerceAtLeast(0)
            UiStyleManager.save(requireContext(), newTheme, newFont, newSize)
            requireActivity().recreate()
            Toast.makeText(requireContext(), "تم حفظ الإعدادات", Toast.LENGTH_SHORT).show()
        }

        binding.exportJsonBtn.setOnClickListener {
            vm.prepareExportData { rows ->
                val file = BackupManager.exportJson(requireContext(), rows)
                BackupManager.shareFile(requireContext(), file)
            }
        }

        binding.exportExcelBtn.setOnClickListener {
            vm.prepareExportData { rows ->
                val file = BackupManager.exportExcel(requireContext(), rows)
                BackupManager.shareFile(requireContext(), file)
            }
        }

        binding.exportWordBtn.setOnClickListener {
            vm.prepareExportData { rows ->
                val file = BackupManager.exportWord(requireContext(), rows)
                BackupManager.shareFile(requireContext(), file)
            }
        }
        binding.shareWhatsappBtn.setOnClickListener {
            vm.prepareExportData { rows ->
                val file = BackupManager.exportJson(requireContext(), rows)
                BackupManager.shareFile(requireContext(), file, "com.whatsapp")
            }
        }
        binding.shareTelegramBtn.setOnClickListener {
            vm.prepareExportData { rows ->
                val file = BackupManager.exportJson(requireContext(), rows)
                BackupManager.shareFile(requireContext(), file, "org.telegram.messenger")
            }
        }

        binding.importJsonBtn.setOnClickListener {
            pendingImportType = "json"
            importer.launch(arrayOf("application/json", "text/plain"))
        }
        binding.importExcelBtn.setOnClickListener {
            pendingImportType = "excel"
            importer.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        }
    }

    private fun securePrefs() = EncryptedSharedPreferences.create(
        requireContext(),
        "secure_settings",
        MasterKey.Builder(requireContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
