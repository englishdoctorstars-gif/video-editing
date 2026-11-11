package com.englishdoctor.vocabsolution

import com.englishdoctor.vocabsolution.data.ConfusedPair
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
        dir.listFiles()!!.forEach { f ->
            val m = bad.find(f.readText())
            assertTrue("${f.name}: '${m?.let { f.readText().substring((it.range.first - 10).coerceAtLeast(0), it.range.last + 10) }}'", m == null)
        }
    }
}
