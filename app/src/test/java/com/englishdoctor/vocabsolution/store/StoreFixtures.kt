package com.englishdoctor.vocabsolution.store

import android.content.Context
import com.englishdoctor.vocabsolution.AppContainer
import com.englishdoctor.vocabsolution.data.Progress
import com.englishdoctor.vocabsolution.domain.Placement
import com.englishdoctor.vocabsolution.domain.Srs
import java.time.LocalDate

/** A believable learner a few weeks in, for store screenshots. */
fun storeApp(context: Context): AppContainer {
    val app = AppContainer(context)
    val today = LocalDate.now().toEpochDay()
    val items = app.content.studyItems
    val cards = items.take(140).mapIndexed { i, it ->
        it.key to Srs.learn(today - 2).copy(stage = 1 + i % 4, due = if (i % 3 == 0) today else today + 3)
    }.toMap() + items.drop(140).take(60).associate { it.key to Srs.learn(today - 40).copy(stage = Srs.MASTERED, due = Long.MAX_VALUE) }
    app.progress.preview(
        Progress(
            onboarded = true, name = "রাফি", goal = "ielts", dailyGoal = 10, placementLevel = Placement.INTERMEDIATE,
            cards = cards, bookmarks = items.take(12).map { it.key }.toSet(), xp = 2860, streak = 12, bestStreak = 12,
            lastActiveDay = today, todayDay = today, todayCount = 7, examBest = mapOf(1 to 23, 2 to 21, 4 to 19),
        ),
    )
    return app
}
