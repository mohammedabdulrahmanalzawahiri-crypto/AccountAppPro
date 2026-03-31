package com.abdelrahman.accountpromax.ui.lock

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.abdelrahman.accountpromax.databinding.ActivityLockBinding
import com.abdelrahman.accountpromax.ui.main.MainActivity
import com.abdelrahman.accountpromax.utils.UiStyleManager

class LockActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UiStyleManager.apply(this)
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = securePrefs()
        val lockEnabled = prefs.getBoolean("lock_enabled", false)
        if (!lockEnabled) {
            goMain()
            return
        }

        binding.unlockButton.setOnClickListener {
            val entered = binding.passwordInput.text?.toString().orEmpty()
            val encoded = Base64.encodeToString(entered.toByteArray(), Base64.NO_WRAP)
            val saved = prefs.getString("password_b64", "") ?: ""
            if (encoded == saved) {
                goMain()
            } else {
                Toast.makeText(this, "كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun securePrefs() = EncryptedSharedPreferences.create(
        this,
        "secure_settings",
        MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
