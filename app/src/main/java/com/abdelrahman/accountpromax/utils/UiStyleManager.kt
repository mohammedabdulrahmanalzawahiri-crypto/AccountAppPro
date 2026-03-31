package com.abdelrahman.accountpromax.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatDelegate

object UiStyleManager {
    private const val PREFS = "ui_settings"

    fun themeNames() = listOf("Ocean", "Forest", "Sunset", "Rose", "Sky", "Night", "Mint", "Orange", "Indigo", "Gray")
    fun fontNames() = listOf("Default", "Sans", "Serif", "Mono")
    fun sizeNames() = listOf("XS", "S", "M", "L", "XL", "XXL")

    fun save(context: Context, theme: Int, font: Int, size: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("theme_index", theme.coerceIn(0, 9))
            .putInt("font_index", font.coerceIn(0, 3))
            .putInt("size_index", size.coerceIn(0, 5))
            .apply()
    }

    fun current(context: Context): Triple<Int, Int, Int> {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Triple(
            p.getInt("theme_index", 0),
            p.getInt("font_index", 0),
            p.getInt("size_index", 2)
        )
    }

    fun apply(activity: Activity) {
        val (theme, _, size) = current(activity)
        val mode = if (theme == 5) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        AppCompatDelegate.setDefaultNightMode(mode)
        val color = intArrayOf(
            0xFF1565C0.toInt(), 0xFF2E7D32.toInt(), 0xFFEF6C00.toInt(), 0xFFC2185B.toInt(),
            0xFF0288D1.toInt(), 0xFF263238.toInt(), 0xFF00695C.toInt(), 0xFFF57C00.toInt(),
            0xFF3949AB.toInt(), 0xFF546E7A.toInt()
        )[theme]
        activity.window.statusBarColor = color
        activity.window.navigationBarColor = color

        val scale = floatArrayOf(0.85f, 0.93f, 1.0f, 1.08f, 1.16f, 1.24f)[size]
        val cfg = Configuration(activity.resources.configuration)
        cfg.fontScale = scale
        activity.resources.updateConfiguration(cfg, activity.resources.displayMetrics)
    }

    fun typeface(context: Context): Typeface {
        return when (current(context).second) {
            1 -> Typeface.SANS_SERIF
            2 -> Typeface.SERIF
            3 -> Typeface.MONOSPACE
            else -> Typeface.DEFAULT
        }
    }
}
