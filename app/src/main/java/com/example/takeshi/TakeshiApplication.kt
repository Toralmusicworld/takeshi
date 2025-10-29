package com.example.takeshi

import android.app.Application
import com.example.takeshi.playback.MusicController

class TakeshiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val theme = ThemeHelper.getThemePreference(this)
        ThemeHelper.applyTheme(theme)
        MusicController.initialize(this)
    }
}
