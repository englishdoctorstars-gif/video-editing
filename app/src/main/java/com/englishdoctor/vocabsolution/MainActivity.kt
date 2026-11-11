package com.englishdoctor.vocabsolution

import android.content.Intent
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
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.VocabRoot
import com.englishdoctor.vocabsolution.widget.EXTRA_OPEN_HOOK
import com.englishdoctor.vocabsolution.widget.WordWidget
import com.englishdoctor.vocabsolution.ui.theme.VocabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var ready by mutableStateOf(false)

    /** Route requested from outside the app (the home-screen widget). */
    private var pendingRoute by mutableStateOf<String?>(null)

    private fun readIntent(intent: Intent?) {
        val hook = intent?.getIntExtra(EXTRA_OPEN_HOOK, -1) ?: -1
        if (hook >= 0) {
            pendingRoute = Routes.hook(hook)
            intent?.removeExtra(EXTRA_OPEN_HOOK)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        readIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { !ready }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as VocabApp).container
        readIntent(intent)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                container.progress.load()
                container.content.studyItems // warm up the content index
            }
            ready = true
            WordWidget.refresh(this@MainActivity)
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
                VocabRoot(container, pendingRoute) { pendingRoute = null }
            }
        }
    }
}
