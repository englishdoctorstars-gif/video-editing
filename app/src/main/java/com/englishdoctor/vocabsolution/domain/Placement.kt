package com.englishdoctor.vocabsolution.domain

/**
 * Onboarding placement test: 10 questions in three bands of difficulty.
 * Harder questions are worth more, so one lucky guess on an easy question
 * cannot place a learner too high.
 */
object Placement {
    const val BEGINNER = "beginner"
    const val INTERMEDIATE = "intermediate"
    const val ADVANCED = "advanced"

    /** Questions per band: easy (book part 1-2 basics), medium (usage), hard (rare words / IELTS). */
    val BAND_SIZES = linkedMapOf(1 to 3, 2 to 3, 3 to 4)
    private val WEIGHT = mapOf(1 to 1, 2 to 2, 3 to 3)
    val MAX_SCORE = BAND_SIZES.entries.sumOf { (band, n) -> n * WEIGHT.getValue(band) } // 21

    /** correctByBand: band (1..3) → number answered correctly. */
    fun score(correctByBand: Map<Int, Int>): Int = correctByBand.entries.sumOf { (band, n) -> n * WEIGHT.getValue(band) }

    fun level(correctByBand: Map<Int, Int>): String {
        val s = score(correctByBand)
        return when {
            s >= 15 -> ADVANCED
            s >= 7 -> INTERMEDIATE
            else -> BEGINNER
        }
    }

    val LEVEL_BN = mapOf(BEGINNER to "প্রাথমিক", INTERMEDIATE to "মধ্যম", ADVANCED to "উন্নত")

    val ADVICE_BN = mapOf(
        BEGINNER to "শব্দ গঠনের ভিত দিয়ে শুরু করুন: অধ্যায় ১ (Word Building) ও ৩ (Memory Hooks)। প্রতিদিন অল্প অল্প করে এগোলেই দ্রুত উন্নতি হবে।",
        INTERMEDIATE to "ভিত ভালো আছে। এবার সঠিক প্রয়োগে জোর দিন: অধ্যায় ৪ (Confused Words), ৫ (Equivalents) ও ৬ (Collocations)। পরীক্ষার মধ্যম স্তরও খুলে দেওয়া হলো।",
        ADVANCED to "চমৎকার! কঠিন শব্দ ও IELTS অংশে সরাসরি যেতে পারেন: অধ্যায় ৮ (Read Smart) ও ৯ (Paraphrase Bank)। পরীক্ষার সব স্তর খুলে দেওয়া হলো।",
    )
}
