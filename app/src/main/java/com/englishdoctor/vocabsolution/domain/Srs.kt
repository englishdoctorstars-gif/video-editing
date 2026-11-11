package com.englishdoctor.vocabsolution.domain

import kotlinx.serialization.Serializable

/**
 * The book's 1-3-7-30 repetition method.
 *
 * Stage 1 = learned on day 1, then reviewed on day 3 (stage 2), day 7 (stage 3) and
 * day 30 (stage 4). Passing the day-30 review makes the word permanent (MASTERED).
 * Days are counted as epoch days so the schedule is independent of time of day.
 */
object Srs {
    const val NEW = 0
    const val MASTERED = 5

    /** Gap in days from stage n to the next review: day1→day3, day3→day7, day7→day30. */
    private val GAP = mapOf(1 to 2, 2 to 4, 3 to 23)

    val STAGE_NAMES = listOf("নতুন", "শিখছি · ১ম দিন", "রিভিউ · ৩য় দিন", "প্রয়োগ · ৭ম দিন", "ঝালাই · ৩০তম দিন", "স্থায়ী")

    enum class Grade { REMEMBERED, HARD, FORGOT }

    fun learn(today: Long): CardState = CardState(stage = 1, due = today + GAP.getValue(1), lastReviewed = today)

    /**
     * REMEMBERED moves to the next stage; HARD keeps the stage and asks again tomorrow;
     * FORGOT restarts the cycle from day 1 (as the book recommends).
     */
    fun review(state: CardState, grade: Grade, today: Long): CardState = when (grade) {
        Grade.REMEMBERED -> {
            val next = (state.stage + 1).coerceAtMost(MASTERED)
            val gap = GAP[next]
            state.copy(stage = next, due = if (gap == null) Long.MAX_VALUE else today + gap,
                lastReviewed = today, correct = state.correct + 1)
        }
        Grade.HARD -> state.copy(due = today + 1, lastReviewed = today, correct = state.correct + 1)
        Grade.FORGOT -> state.copy(stage = 1, due = today + 1, lastReviewed = today, wrong = state.wrong + 1)
    }

    fun isDue(state: CardState, today: Long) = state.stage in 1 until MASTERED && state.due <= today
}

@Serializable
data class CardState(
    val stage: Int,
    val due: Long,
    val lastReviewed: Long,
    val correct: Int = 0,
    val wrong: Int = 0,
)
