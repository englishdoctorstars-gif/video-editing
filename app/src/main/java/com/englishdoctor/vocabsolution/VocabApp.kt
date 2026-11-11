package com.englishdoctor.vocabsolution

import android.app.Application
import android.content.Context
import com.englishdoctor.vocabsolution.data.ContentRepository
import com.englishdoctor.vocabsolution.data.ProgressStore
import com.englishdoctor.vocabsolution.tts.Speaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Manual dependency container: small app, no DI framework needed yet. */
class AppContainer(app: Context) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val content = ContentRepository(app)
    val progress = ProgressStore(app, scope)
    val speaker by lazy { Speaker(app) }
}

class VocabApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
