package com.englishdoctor.vocabsolution.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ContentRepository
import com.englishdoctor.vocabsolution.data.OPTION_KEYS
import com.englishdoctor.vocabsolution.data.OPTION_LABELS
import com.englishdoctor.vocabsolution.domain.Placement
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.ProgressRing
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.learn.OptionCard
import com.englishdoctor.vocabsolution.ui.learn.QuizQuestion
import com.englishdoctor.vocabsolution.ui.learn.fillTheBlankQuestions
import com.englishdoctor.vocabsolution.ui.learn.makeQuestion
import com.englishdoctor.vocabsolution.ui.learn.partColor
import com.englishdoctor.vocabsolution.ui.theme.Pos

data class BandQuestion(val band: Int, val q: QuizQuestion)

/** Ten questions drawn from across the book, easy → hard. */
fun buildPlacement(c: ContentRepository): List<BandQuestion> {
    val out = mutableListOf<BandQuestion>()
    // Band 1: word-building and everyday English.
    val posBn = listOf("noun", "verb", "adjective", "adverb").associateWith { Pos.of(it).let { p -> "${p.symbol} ${p.bn}" } }
    c.suffixes.filter { it.posFormed in posBn }.random().let { s ->
        val opts = posBn.values.toList()
        out += BandQuestion(1, QuizQuestion("${s.affix} দিয়ে শেষ হওয়া শব্দ (যেমন ${s.examples.first()}) সাধারণত কোন পদ?", opts, opts.indexOf(posBn.getValue(s.posFormed!!))))
    }
    c.collocations.filter { it.section == "power_verbs" }.random().let { col ->
        out += BandQuestion(1, makeQuestion("‘${col.phrase}’-এর অর্থ কী?", col.meaningBn, c.collocations, { it.meaningBn }))
    }
    c.collocationMistakes.random().let { m ->
        val opts = listOf(m.wrong, m.right).shuffled()
        out += BandQuestion(1, QuizQuestion("“${m.meaningBn}” — কোনটি সঠিক ইংরেজি?", opts, opts.indexOf(m.right)))
    }
    // Band 2: correct usage.
    fillTheBlankQuestions(c.pairs, 2).forEach { out += BandQuestion(2, it) }
    c.synAnts.random().let { s ->
        out += BandQuestion(2, makeQuestion("‘${s.word}’-এর সমার্থক শব্দ কোনটি?", s.synonyms.first(),
            c.synAnts.flatMap { it.synonyms + it.antonyms }.filter { it !in s.synonyms }, { it }))
    }
    // Band 3: rare words and academic English.
    c.hooks.shuffled().take(2).forEach { h ->
        out += BandQuestion(3, makeQuestion("‘${h.word}’ শব্দের অর্থ কী?", h.meaningBn, c.hooks, { it.meaningBn }))
    }
    c.paraphrases.random().let { p ->
        out += BandQuestion(3, makeQuestion("“${p.paraphrases.first()}” — কোনটির paraphrase?", p.collocation,
            c.paraphrases.filter { it.topicNo == p.topicNo }, { it.collocation }))
    }
    c.oneWords.random().let { o ->
        out += BandQuestion(3, makeQuestion(o.descriptionEn, o.answer, c.oneWords.filter { it.categoryEn == o.categoryEn }, { it.answer }))
    }
    return out
}

/**
 * The test itself. No answers are revealed during the test (it measures, not teaches);
 * the result screen shows the level and where to start.
 */
@Composable
fun PlacementTest(onFinished: (level: String, score: Int) -> Unit, onSkip: (() -> Unit)?, modifier: Modifier = Modifier) {
    val app = LocalApp.current
    var started by remember { mutableStateOf(false) }
    val questions = remember { buildPlacement(app.content) }
    var index by remember { mutableIntStateOf(0) }
    var chosen by remember { mutableStateOf<Int?>(null) }
    val correct = remember { mutableStateMapOf<Int, Int>() }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (!started) {
            Text("স্তর যাচাই পরীক্ষা", style = MaterialTheme.typography.headlineSmall)
            Text("বইয়ের বিভিন্ন অধ্যায় থেকে ১০টি প্রশ্ন, সহজ থেকে কঠিন। মাত্র ২–৩ মিনিট লাগবে। ফলাফল দেখে অ্যাপ আপনার জন্য সঠিক শুরুর জায়গা ঠিক করবে।")
            Text("না জানলে চিন্তা নেই, যেটি সবচেয়ে কাছের মনে হয় সেটি বেছে এগিয়ে যান। এটি পাস-ফেলের পরীক্ষা নয়, শুধু আপনার অবস্থান জানার উপায়।",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button({ started = true }, Modifier.fillMaxWidth()) { Text("শুরু করুন") }
            if (onSkip != null) OutlinedButton(onSkip, Modifier.fillMaxWidth()) { Text("এখন নয়, পরে দেব") }
            return@Column
        }
        if (index >= questions.size) {
            val level = Placement.level(correct)
            val score = Placement.score(correct)
            val color = partColor(when (level) { Placement.BEGINNER -> 1; Placement.INTERMEDIATE -> 2; else -> 3 })
            ContentCard(accent = color) {
                ProgressRing(score.toFloat() / Placement.MAX_SCORE, color, size = 88.dp, stroke = 8.dp) {
                    Text("${score.bn()}/${Placement.MAX_SCORE.bn()}", style = MaterialTheme.typography.titleMedium)
                }
                Text("আপনার স্তর: ${Placement.LEVEL_BN[level]}", style = MaterialTheme.typography.headlineSmall, color = color,
                    modifier = Modifier.padding(top = 10.dp))
                Text(Placement.ADVICE_BN.getValue(level), modifier = Modifier.padding(top = 6.dp))
            }
            Button({ onFinished(level, score) }, Modifier.fillMaxWidth()) { Text("এগিয়ে চলুন 🚀") }
            return@Column
        }
        val bq = questions[index]
        LinearProgressIndicator(progress = { index.toFloat() / questions.size }, Modifier.fillMaxWidth())
        Text("প্রশ্ন ${(index + 1).bn()} / ${questions.size.bn()} · ${listOf("", "সহজ", "মাঝারি", "কঠিন")[bq.band]}",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(bq.q.prompt, style = MaterialTheme.typography.titleLarge)
        bq.q.options.forEachIndexed { i, opt ->
            OptionCard("${OPTION_LABELS[OPTION_KEYS[i]]}) $opt", null, selected = chosen == i) { chosen = i }
        }
        Button({
            if (chosen == bq.q.correct) correct[bq.band] = (correct[bq.band] ?: 0) + 1
            chosen = null
            index++
        }, Modifier.fillMaxWidth(), enabled = chosen != null) { Text(if (index + 1 < questions.size) "পরের প্রশ্ন" else "ফলাফল দেখুন") }
    }
}

/** Retake from the profile screen. */
@Composable
fun PlacementScreen(nav: NavHostController) {
    val app = LocalApp.current
    ScreenScaffold("স্তর যাচাই", onBack = { nav.popBackStack() }) { m ->
        PlacementTest(onFinished = { level, score -> app.progress.recordPlacement(level, score); nav.popBackStack() }, onSkip = null, modifier = m)
    }
}
