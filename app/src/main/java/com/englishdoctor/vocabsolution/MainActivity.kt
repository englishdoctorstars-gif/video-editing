package com.englishdoctor.vocabsolution

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.englishdoctor.vocabsolution.notify.ReminderWorker
import com.englishdoctor.vocabsolution.ui.VocabRoot
import com.englishdoctor.vocabsolution.ui.theme.VocabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var ready by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !ready }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as VocabApp).container
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                container.progress.load()
                container.content.studyItems // warm up the content index
            }
            ready = true
        }
        setContent {
            if (!ready) return@setContent
            val progress by container.progress.state.collectAsStateWithLifecycle()
            val dark = when (progress.settings.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            LaunchedEffect(progress.settings.ttsRate) { container.speaker.rate = progress.settings.ttsRate }
            LaunchedEffect(progress.onboarded, progress.settings.reminderEnabled, progress.settings.reminderHour) {
                if (progress.onboarded && progress.settings.reminderEnabled) {
                    ReminderWorker.schedule(this@MainActivity, progress.settings.reminderHour)
                } else {
                    ReminderWorker.cancel(this@MainActivity)
                }
            }
            VocabTheme(dark = dark, fontScale = progress.settings.fontScale) {
                VocabRoot(container)
            }
        }
    }
}
