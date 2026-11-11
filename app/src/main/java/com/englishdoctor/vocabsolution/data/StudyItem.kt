package com.englishdoctor.vocabsolution.data

/**
 * A single reviewable card, independent of which chapter it came from.
 * [key] is stable ("hook:hook_001") and is what progress is stored against.
 */
data class StudyItem(
    val key: String,
    val module: ModuleId,
    val title: String,
    val pos: String?,
    val subtitle: String,
    val answer: String,
    val detail: String = "",
    val speak: String = title,
) {
    val searchText: String = "$title $subtitle $answer".lowercase()
}

enum class ModuleId(
    val number: Int,
    val titleEn: String,
    val titleBn: String,
    val part: Int,
    val available: Boolean,
) {
    WORD_BUILDING(1, "Word Building Basics", "শব্দ ভাঙা ও গড়ার কৌশল", 1, true),
    ROOT_POWER(2, "Root Power", "এক মূল, অনেক শব্দ", 1, true),
    MEMORY_HOOKS(3, "Memory Hooks", "গল্পে গল্পে শব্দ মনে রাখা", 1, true),
    CONFUSED(4, "Commonly Confused Words", "পার্থক্য কী?", 2, true),
    EQUIVALENTS(5, "Word Equivalents", "সমার্থক, বিপরীত ও এক কথায় প্রকাশ", 2, true),
    COLLOCATIONS(6, "Everyday Collocations", "নিত্যদিনের ২০০ শব্দজুটি", 2, false),
    LISTEN(7, "Listen Smart", "শুনে বোঝার শব্দভান্ডার", 3, false),
    READ(8, "Read Smart", "পড়ে বোঝার শব্দভান্ডার", 3, false),
    PARAPHRASE(9, "The Paraphrase Bank", "একই কথা, ভিন্ন ভাষায়", 3, false),
    WRITE(10, "Write Smart", "লেখায় উচ্চ Band-এর শব্দ", 3, false),
    EXAM(11, "Exam Ready", "চাকরির পরীক্ষার ১০টি MCQ সেট", 4, true),
}

val PART_TITLES = mapOf(
    1 to "প্রথম পর্ব · শব্দশক্তির ভিত",
    2 to "দ্বিতীয় পর্ব · সঠিক শব্দ, সঠিক প্রয়োগ",
    3 to "তৃতীয় পর্ব · IELTS শব্দভান্ডার",
    4 to "চতুর্থ পর্ব · চাকরির পরীক্ষার প্রস্তুতি",
)
