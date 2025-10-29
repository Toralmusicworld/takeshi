package com.example.takeshi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.takeshi.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import com.example.takeshi.databinding.MiniPlayerBinding
import com.example.takeshi.playback.MusicController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var miniPlayerBinding: MiniPlayerBinding

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                loadMusic()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        miniPlayerBinding = MiniPlayerBinding.bind(binding.root.findViewById(R.id.mini_player))

        setSupportActionBar(binding.topAppBar)

        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            binding.drawerLayout.closeDrawer(GravityCompat.START)

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }
                R.id.nav_hidden_folder -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HiddenFolderFragment())
                        .addToBackStack(null) // Optional: Add to back stack
                        .commit()
                }
                R.id.nav_playlists -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PlaylistsFragment())
                        .addToBackStack(null)
                        .commit()
                }
                R.id.nav_browser -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, BrowserFragment())
                        .addToBackStack(null)
                        .commit()
                }
                R.id.nav_theme -> showThemeDialog()
                // Add other fragments here
            }
            true
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
            binding.navigationView.setCheckedItem(R.id.nav_home)
        }

        checkPermissions()
        setupMiniPlayer()
        observePlayerState()
    }

    private fun setupMiniPlayer() {
        miniPlayerBinding.root.setOnClickListener {
            startActivity(Intent(this, NowPlayingActivity::class.java))
        }
        miniPlayerBinding.miniPlayerPlayPause.setOnClickListener {
            MusicController.mediaController?.playWhenReady = !MusicController.mediaController?.playWhenReady!!
        }
    }

    private fun observePlayerState() {
        MusicController.mediaController?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                updateMiniPlayerUi(mediaItem)
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                miniPlayerBinding.miniPlayerPlayPause.setImageResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
        })
        updateMiniPlayerUi(MusicController.mediaController?.currentMediaItem)
    }

    private fun updateMiniPlayerUi(mediaItem: androidx.media3.common.MediaItem?) {
        if (mediaItem != null) {
            miniPlayerBinding.miniPlayerTitle.text = mediaItem.mediaMetadata.title
        } else {
            miniPlayerBinding.miniPlayerTitle.text = "Not Playing"
        }
    }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadMusic()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                // showInContextUI(...)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun loadMusic() {
        // This will be handled by the fragment, but we trigger it from here
        // after permission is granted.
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is HomeFragment) {
                fragment.loadMusic()
            }
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_driving_mode -> {
                startActivity(Intent(this, DrivingModeActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        val currentTheme = ThemeHelper.getThemePreference(this)
        val checkedItem = when (currentTheme) {
            ThemeHelper.LIGHT_MODE -> 0
            ThemeHelper.DARK_MODE -> 1
            else -> 2
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val theme = when (which) {
                    0 -> ThemeHelper.LIGHT_MODE
                    1 -> ThemeHelper.DARK_MODE
                    else -> ThemeHelper.DEFAULT_MODE
                }
                ThemeHelper.saveThemePreference(this, theme)
                ThemeHelper.applyTheme(theme)
                dialog.dismiss()
            }
            .show()
    }
}
