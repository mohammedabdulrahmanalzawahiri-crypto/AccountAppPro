package com.abdelrahman.accountpromax.ui.about

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.abdelrahman.accountpromax.BuildConfig
import com.abdelrahman.accountpromax.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        binding.buildInfoText.text = "الإصدار: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\nآخر فتح: $date"
        binding.footerYear.post {
            val paint = binding.footerYear.paint
            val width = paint.measureText("2030")
            paint.shader = LinearGradient(
                0f, 0f, width, binding.footerYear.textSize,
                intArrayOf(0xFFFF8A00.toInt(), 0xFFE52E71.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
            binding.footerYear.invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
