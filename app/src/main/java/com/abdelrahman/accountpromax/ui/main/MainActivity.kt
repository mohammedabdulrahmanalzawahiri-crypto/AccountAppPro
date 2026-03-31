package com.abdelrahman.accountpromax.ui.main

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.GestureDetectorCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.abdelrahman.accountpromax.R
import com.abdelrahman.accountpromax.databinding.ActivityMainBinding
import com.abdelrahman.accountpromax.utils.BackupManager
import com.abdelrahman.accountpromax.utils.UiStyleManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val vm by viewModels<MainViewModel>()
    private lateinit var gestureDetector: GestureDetectorCompat
    private var drawerTouchStartX: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UiStyleManager.apply(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        val navController = navHost.navController
        NavigationUI.setupWithNavController(binding.navigationView, navController)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar, R.string.app_name, R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        vm.ensureDefaultProject()
        vm.projects.observe(this) { list ->
            if (list.isEmpty()) return@observe
            val selected = vm.selectedProjectId.value ?: list.first().id
            val project = list.firstOrNull { it.id == selected } ?: list.first()
            vm.selectProject(project.id)
            binding.toolbar.subtitle = "المشروع: ${project.name}"
        }
        initProjectSwipe()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun initProjectSwipe() {
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val first = e1 ?: return false
                val dx = e2.x - first.x
                if (kotlin.math.abs(dx) < 120 || kotlin.math.abs(velocityX) < 300) return false
                val list = vm.projects.value ?: return false
                if (list.isEmpty()) return false
                val currentId = vm.selectedProjectId.value ?: list.first().id
                val idx = list.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
                val next = if (dx < 0) (idx + 1).coerceAtMost(list.lastIndex) else (idx - 1).coerceAtLeast(0)
                vm.selectProject(list[next].id)
                binding.toolbar.subtitle = "المشروع: ${list[next].name}"
                return true
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> drawerTouchStartX = ev.x
                MotionEvent.ACTION_UP -> {
                    val dx = ev.x - drawerTouchStartX
                    if (dx > 80f) binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
        }
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onStop() {
        super.onStop()
        // Auto-backup a lightweight JSON snapshot when app goes background.
        vm.prepareExportData { rows ->
            BackupManager.exportJson(this, rows)
        }
    }
}
