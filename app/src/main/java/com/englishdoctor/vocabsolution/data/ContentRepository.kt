package com.englishdoctor.vocabsolution.data

import android.content.Context
import kotlinx.serialization.json.Json

/** Loads the offline book content from assets once and keeps it in memory (~700 KB of JSON). */
class ContentRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private inline fun <reified T> load(name: String): List<T> =
        context.assets.open("content/$name.json").bufferedReader().use { json.decodeFromString(it.readText()) }

    val suffixes: List<Suffix> by lazy { load("suffixes") }
    val prefixes: List<Prefix> by lazy { load("prefixes") }
    val analyses: List<WordAnalysis> by lazy { load("word_analysis") }
    val multiplying: List<MultiplyingWord> by lazy { load("multiplying_words") }
    val roots: List<RootFamily> by lazy { load("root_families") }
    val hooks: List<MemoryHook> by lazy { load("memory_hooks") }
    val pairs: List<ConfusedPair> by lazy { load("confused_pairs") }
    val synAnts: List<SynAnt> by lazy { load("synonyms_antonyms") }
    val oneWords: List<OneWord> by lazy { load("one_word") }
    val mcqs: List<Mcq> by lazy { load("mcq") }

    fun mcqSet(set: Int): List<Mcq> = mcqs.filter { it.set == set }.sortedBy { it.number }

    fun root(id: String) = roots.first { it.id == id }
    fun hook(id: String) = hooks.first { it.id == id }
    fun pair(id: String) = pairs.first { it.id == id }

    /** Find the root family a word belongs to (used for "পরিবার দেখুন" links). */
    fun rootFor(word: String): RootFamily? {
        val w = word.lowercase()
        return roots.firstOrNull { f -> (f.rootPlusSuffix + f.prefixPlusRoot).any { it.word.lowercase() == w } }
    }

    /** Every reviewable card in the app, keyed for progress tracking. */
    val studyItems: List<StudyItem> by lazy {
        buildList {
            suffixes.forEach {
                add(StudyItem("suffix:${it.id}", ModuleId.WORD_BUILDING, it.affix, it.posFormed,
                    "প্রত্যয় · উদাহরণ: ${it.examples.joinToString(", ")}", it.meaningBn, speak = it.examples.firstOrNull() ?: it.affix))
            }
            prefixes.forEach {
                add(StudyItem("prefix:${it.id}", ModuleId.WORD_BUILDING, it.affix, null,
                    "উপসর্গ · উদাহরণ: ${it.examples.joinToString(", ")}", it.meaningBn, speak = it.examples.firstOrNull() ?: it.affix))
            }
            analyses.forEach {
                add(StudyItem("analysis:${it.id}", ModuleId.WORD_BUILDING, it.word, it.pos,
                    "শব্দটি ভাঙুন", it.meaningBn, detail = it.parts.joinToString(" + ")))
            }
            roots.forEach { f ->
                (f.rootPlusSuffix + f.prefixPlusRoot).forEach { w ->
                    add(StudyItem("root:${f.id}:${w.word}", ModuleId.ROOT_POWER, w.word, w.pos,
                        "মূল: ${f.root}", w.meaningBn))
                }
            }
            hooks.forEach {
                add(StudyItem("hook:${it.id}", ModuleId.MEMORY_HOOKS, it.word, it.pos,
                    "${it.ipa} · ${it.bnPronunciation}", it.meaningBn,
                    detail = "ধ্বনি-সূত্র: ${it.soundHookBn}\nমূল সূত্র: ${it.keyCueBn}"))
            }
            pairs.forEach {
                add(StudyItem("pair:${it.id}", ModuleId.CONFUSED, "${it.a.word} vs ${it.b.word}", null,
                    "পার্থক্য কী?", "${it.a.word}: ${it.a.meaningBn}\n${it.b.word}: ${it.b.meaningBn}",
                    detail = it.differenceBn, speak = "${it.a.word}, ${it.b.word}"))
            }
            synAnts.forEach {
                add(StudyItem("synant:${it.id}", ModuleId.EQUIVALENTS, it.word, it.pos,
                    "সমার্থক ও বিপরীত", it.meaningBn,
                    detail = "≈ ${it.synonyms.joinToString(", ")}\n↔ ${it.antonyms.joinToString(", ")}"))
            }
            oneWords.forEach {
                add(StudyItem("oneword:${it.id}", ModuleId.EQUIVALENTS, it.descriptionEn, null,
                    "এক কথায় প্রকাশ · ${it.categoryBn}", "${it.answer} — ${it.meaningBn}", speak = it.answer))
            }
        }
    }

    private val itemsByKey by lazy { studyItems.associateBy { it.key } }
    fun item(key: String): StudyItem? = itemsByKey[key] ?: mcqItem(key)

    /** Wrong MCQ answers become review cards too. */
    private fun mcqItem(key: String): StudyItem? {
        if (!key.startsWith("mcq:")) return null
        val q = mcqs.firstOrNull { it.id == key.removePrefix("mcq:") } ?: return null
        return StudyItem(key, ModuleId.EXAM, q.question, null, MCQ_TYPE_BN[q.type] ?: "",
            "${OPTION_LABELS[q.answer]}) ${q.options[q.answer]}", speak = q.options[q.answer] ?: "")
    }

    fun search(query: String, limit: Int = 80): List<StudyItem> {
        val q = query.trim().lowercase()
        if (q.length < 2) return emptyList()
        val starts = studyItems.filter { it.title.lowercase().startsWith(q) }
        val startKeys = starts.mapTo(HashSet()) { it.key }
        val contains = studyItems.filter { it.key !in startKeys && it.searchText.contains(q) }
        return (starts + contains).take(limit)
    }

    fun wordOfTheDay(dayIndex: Long): MemoryHook = hooks[(dayIndex % hooks.size).toInt()]
}
