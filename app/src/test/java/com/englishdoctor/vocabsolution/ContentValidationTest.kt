package com.englishdoctor.vocabsolution

import com.englishdoctor.vocabsolution.data.Collocation
import com.englishdoctor.vocabsolution.data.CommonMistake
import com.englishdoctor.vocabsolution.data.ConfusedPair
import com.englishdoctor.vocabsolution.data.ListeningWord
import com.englishdoctor.vocabsolution.data.MapWord
import com.englishdoctor.vocabsolution.data.NumberTip
import com.englishdoctor.vocabsolution.data.Paraphrase
import com.englishdoctor.vocabsolution.data.ReadingPassage
import com.englishdoctor.vocabsolution.data.Signpost
import com.englishdoctor.vocabsolution.data.SpellingTrap
import com.englishdoctor.vocabsolution.data.Writing
import com.englishdoctor.vocabsolution.data.MemoryHook
import com.englishdoctor.vocabsolution.data.Mcq
import com.englishdoctor.vocabsolution.data.OneWord
import com.englishdoctor.vocabsolution.data.Prefix
import com.englishdoctor.vocabsolution.data.RootFamily
import com.englishdoctor.vocabsolution.data.Suffix
import com.englishdoctor.vocabsolution.data.SynAnt
import com.englishdoctor.vocabsolution.data.WordAnalysis
import com.englishdoctor.vocabsolution.data.MultiplyingWord
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The bundled JSON must decode and match the counts stated in the book. */
class ContentValidationTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val dir = listOf("src/main/assets/content", "app/src/main/assets/content").map(::File).first { it.exists() }
    private inline fun <reified T> load(name: String): List<T> = json.decodeFromString(File(dir, "$name.json").readText())

    @Test
    fun countsMatchTheBook() {
        assertEquals(48, load<Suffix>("suffixes").size)
        assertEquals(48, load<Prefix>("prefixes").size)
        assertEquals(20, load<WordAnalysis>("word_analysis").size)
        assertEquals(120, load<MultiplyingWord>("multiplying_words").size)
        val roots = load<RootFamily>("root_families")
        assertEquals(96, roots.size)
        assertEquals(2694, roots.sumOf { it.size })
        roots.forEach { assertEquals(it.root, it.statedCount, it.size) }
        assertEquals(100, load<MemoryHook>("memory_hooks").size)
        assertEquals(100, load<ConfusedPair>("confused_pairs").size)
        assertEquals(144, load<SynAnt>("synonyms_antonyms").size)
        assertEquals(130, load<OneWord>("one_word").size)
        assertEquals(250, load<Mcq>("mcq").size)
    }

    @Test
    fun chapters6to10MatchTheBook() {
        val coll = load<Collocation>("collocations")
        assertEquals(200, coll.size)
        assertEquals(100, coll.count { it.section == "power_verbs" })
        coll.forEach { c -> listOf(c.phrase, c.meaningBn, c.exampleEn, c.exampleBn).forEach { assertTrue(c.id, it.isNotBlank()) } }
        assertEquals(25, load<CommonMistake>("collocation_mistakes").size)
        val listen = load<ListeningWord>("listening_words")
        assertEquals(100, listen.size)
        assertEquals(setOf(1, 2, 3, 4), listen.map { it.section }.toSet())
        listen.forEach { assertTrue(it.id, it.meaningBn.isNotBlank() && it.ipa.startsWith("/")) }
        assertEquals(30, load<MapWord>("map_words").size)
        assertEquals(7, load<NumberTip>("number_tips").size)
        assertEquals(59, load<SpellingTrap>("spelling_traps").size)
        assertEquals(17, load<Signpost>("signposts").size)
        val passages = load<ReadingPassage>("reading_passages")
        assertEquals(150, passages.size)
        assertEquals(10, passages.map { it.subjectNo }.distinct().size)
        passages.forEach { assertEquals(it.id, 10, it.vocab.size) }
        assertEquals(1500, passages.sumOf { it.vocab.size })
        val para = load<Paraphrase>("paraphrases")
        assertEquals(1152, para.size)
        para.groupBy { it.topicNo }.forEach { (t, l) -> assertEquals("topic $t", 96, l.size) }
        para.forEach { assertEquals(it.id, 2, it.paraphrases.size) }
        val writing = json.decodeFromString<Writing>(File(dir, "writing.json").readText())
        assertEquals(14, writing.sections.size)
        assertEquals(36, writing.sections.single { it.group == "academic" }.rows.size)
        assertEquals(15, writing.sections.single { it.group == "mistakes" }.rows.size)
        assertEquals(10, writing.sections.single { it.group == "linking" }.rows.size)
        writing.sections.forEach { s -> s.rows.forEach { r -> assertEquals(s.id, s.columns.size, r.size); assertTrue(s.id, r.all { it.isNotBlank() }) } }
        assertEquals(6, writing.topicVocab.size)
        writing.topicVocab.forEach { assertEquals(it.categoryEn, 8, it.items.size) }
    }

    @Test
    fun everyMcqHasFourOptionsAndAValidAnswer() {
        load<Mcq>("mcq").forEach { q ->
            assertEquals(q.id, setOf("ka", "kha", "ga", "gha"), q.options.keys)
            assertTrue(q.id, q.answer in q.options)
        }
    }

    @Test
    fun memoryHooksAreComplete() {
        load<MemoryHook>("memory_hooks").forEach { h ->
            listOf(h.word, h.ipa, h.meaningBn, h.soundHookBn, h.storyBn, h.keyCueBn, h.exampleEn, h.exampleBn)
                .forEach { assertTrue("${h.id} has an empty field", it.isNotBlank()) }
        }
    }

    @Test
    fun noBrokenBengaliGlyphs() {
        // Artifacts of PDF extraction: doubled vowel signs, or a lone vowel sign after a space.
        val bad = Regex("([\\u09BE-\\u09CC])\\1|\\s[\\u09BE-\\u09CC\\u0981-\\u0983]")
        dir.listFiles()!!.filter { it.extension == "json" }.forEach { f ->
            val m = bad.find(f.readText())
            assertTrue("${f.name}: '${m?.let { f.readText().substring((it.range.first - 10).coerceAtLeast(0), it.range.last + 10) }}'", m == null)
        }
    }
}
