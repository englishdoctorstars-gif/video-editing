package com.englishdoctor.vocabsolution.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** British English pronunciation via the device's offline TTS engine (the book uses British IPA). */
class Speaker(context: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    var rate: Float = 0.9f

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val r = tts.setLanguage(Locale.UK)
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) tts.setLanguage(Locale.US)
            ready = true
        }
    }

    fun speak(text: String, slow: Boolean = false) {
        if (!ready || text.isBlank()) return
        tts.setSpeechRate(if (slow) rate * 0.7f else rate)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    fun shutdown() = tts.shutdown()
}
