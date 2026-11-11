package com.englishdoctor.vocabsolution.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.englishdoctor.vocabsolution.domain.CardState
import com.englishdoctor.vocabsolution.domain.Srs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "vocabsolution")
private val KEY = stringPreferencesKey("progress_v1")

@Serializable
data class ExamAttempt(val set: Int, val score: Int, val day: Long, val byType: Map<String, Int> = emptyMap())

@Serializable
data class Settings(
    val theme: String = "system", // system | light | dark
    val fontScale: Float = 1f,
    val ttsRate: Float = 0.9f,
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
)

@Serializable
data class Progress(
    val onboarded: Boolean = false,
    val name: String = "",
    val goal: String = "general", // job | ielts | foundation | general
    val dailyGoal: Int = 10,
    val cards: Map<String, CardState> = emptyMap(),
    val bookmarks: Set<String> = emptySet(),
    val notes: Map<String, String> = emptyMap(),
    val examBest: Map<Int, Int> = emptyMap(),
    val examHistory: List<ExamAttempt> = emptyList(),
    val xp: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDay: Long = 0,
    val todayDay: Long = 0,
    val todayCount: Int = 0,
    val settings: Settings = Settings(),
) {
    fun todayDone(today: Long) = if (todayDay == today) todayCount else 0
    fun dueKeys(today: Long) = cards.filter { Srs.isDue(it.value, today) }.keys
    fun stageCounts(): IntArray = IntArray(Srs.MASTERED + 1).also { a -> cards.values.forEach { a[it.stage]++ } }
    fun currentStreak(today: Long) = if (lastActiveDay >= today - 1) streak else 0
}

/** All learner progress, persisted as one JSON document in DataStore (small: a few thousand cards). */
class ProgressStore(private val context: Context, private val scope: CoroutineScope) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val _state = MutableStateFlow(Progress())
    val state: StateFlow<Progress> = _state.asStateFlow()

    @Volatile
    var loaded = false
        private set

    suspend fun load() {
        val raw = context.dataStore.data.first()[KEY]
        _state.value = raw?.let { runCatching { json.decodeFromString<Progress>(it) }.getOrNull() } ?: Progress()
        loaded = true
    }

    /** For previews and screenshot tests: show a given state without touching storage. */
    fun preview(p: Progress) {
        _state.value = p
        loaded = true
    }

    private fun update(block: (Progress) -> Progress) {
        val next = block(_state.value)
        _state.value = next
        scope.launch { context.dataStore.edit { it[KEY] = json.encodeToString(Progress.serializer(), next) } }
    }

    fun today(): Long = LocalDate.now().toEpochDay()

    /** Counts an activity for the daily goal, XP and streak. */
    private fun Progress.withActivity(xpGain: Int, count: Int = 1): Progress {
        val t = today()
        val newStreak = when (lastActiveDay) {
            t -> streak
            t - 1 -> streak + 1
            else -> 1
        }
        return copy(
            xp = xp + xpGain,
            streak = newStreak,
            bestStreak = maxOf(bestStreak, newStreak),
            lastActiveDay = t,
            todayDay = t,
            todayCount = todayDone(t) + count,
        )
    }

    fun completeOnboarding(name: String, goal: String, dailyGoal: Int) =
        update { it.copy(onboarded = true, name = name.trim(), goal = goal, dailyGoal = dailyGoal) }

    fun isLearned(key: String) = _state.value.cards.containsKey(key)

    fun learn(key: String) = update { p ->
        if (p.cards.containsKey(key)) p
        else p.copy(cards = p.cards + (key to Srs.learn(today()))).withActivity(xpGain = 5)
    }

    fun review(key: String, grade: Srs.Grade) = update { p ->
        val current = p.cards[key] ?: Srs.learn(today())
        val xp = when (grade) { Srs.Grade.REMEMBERED -> 10; Srs.Grade.HARD -> 6; Srs.Grade.FORGOT -> 2 }
        p.copy(cards = p.cards + (key to Srs.review(current, grade, today()))).withActivity(xp)
    }

    fun toggleBookmark(key: String) = update { p ->
        p.copy(bookmarks = if (key in p.bookmarks) p.bookmarks - key else p.bookmarks + key)
    }

    fun setNote(key: String, note: String) = update { p ->
        p.copy(notes = if (note.isBlank()) p.notes - key else p.notes + (key to note))
    }

    fun recordExam(set: Int, score: Int, byType: Map<String, Int>, wrongKeys: List<String>) = update { p ->
        val t = today()
        val cards = p.cards.toMutableMap()
        wrongKeys.forEach { k -> cards[k] = Srs.learn(t).copy(due = t + 1) }
        p.copy(
            examBest = p.examBest + (set to maxOf(score, p.examBest[set] ?: 0)),
            examHistory = (p.examHistory + ExamAttempt(set, score, t, byType)).takeLast(100),
            cards = cards,
        ).withActivity(xpGain = score * 2, count = 0)
    }

    fun updateSettings(block: (Settings) -> Settings) = update { it.copy(settings = block(it.settings)) }

    fun updateProfile(name: String, goal: String, dailyGoal: Int) =
        update { it.copy(name = name.trim(), goal = goal, dailyGoal = dailyGoal) }

    fun resetAll() = update { Progress(onboarded = true, name = it.name, goal = it.goal, dailyGoal = it.dailyGoal, settings = it.settings) }
}
