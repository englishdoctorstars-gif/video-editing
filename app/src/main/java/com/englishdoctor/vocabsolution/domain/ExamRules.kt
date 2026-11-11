package com.englishdoctor.vocabsolution.domain

/**
 * Levels from the book: sets 1-3 primary, 4-7 intermediate, 8-10 advanced.
 * "২০টির বেশি সঠিক হলে পরের স্তরে যান" — scoring more than 20/25 in any set of a level
 * unlocks the next level.
 */
object ExamRules {
    const val QUESTIONS = 25
    const val PASS_ABOVE = 20
    const val MINUTES = 20

    val LEVEL_SETS = linkedMapOf(
        "primary" to (1..3),
        "intermediate" to (4..7),
        "advanced" to (8..10),
    )
    val LEVEL_BN = mapOf(
        "primary" to "প্রাথমিক স্তর",
        "intermediate" to "মধ্যম স্তর",
        "advanced" to "উচ্চ স্তর",
    )
    val LEVEL_HINT = mapOf(
        "primary" to "প্রাথমিক শিক্ষক ও সাধারণ সরকারি চাকরি",
        "intermediate" to "এনটিআরসিএ ও বেসরকারি ব্যাংক",
        "advanced" to "বিসিএস ও বাংলাদেশ ব্যাংক",
    )

    fun levelOf(set: Int) = LEVEL_SETS.entries.first { set in it.value }.key

    /** bestScores: set number → best score. */
    fun isLevelUnlocked(level: String, bestScores: Map<Int, Int>): Boolean {
        val levels = LEVEL_SETS.keys.toList()
        val idx = levels.indexOf(level)
        if (idx <= 0) return true
        val previous = LEVEL_SETS.getValue(levels[idx - 1])
        return previous.any { (bestScores[it] ?: 0) > PASS_ABOVE }
    }

    fun isSetUnlocked(set: Int, bestScores: Map<Int, Int>) = isLevelUnlocked(levelOf(set), bestScores)
}
