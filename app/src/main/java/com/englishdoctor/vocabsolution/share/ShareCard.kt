package com.englishdoctor.vocabsolution.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.englishdoctor.vocabsolution.R
import com.englishdoctor.vocabsolution.data.ConfusedPair
import com.englishdoctor.vocabsolution.data.MemoryHook
import com.englishdoctor.vocabsolution.data.StudyItem
import com.englishdoctor.vocabsolution.ui.theme.Pos
import java.io.File

/** What goes on a shareable word card. */
data class ShareCardData(
    val tag: String,
    val word: String,
    val pos: String? = null,
    val ipa: String = "",
    val meaningBn: String,
    val sections: List<Pair<String, String>> = emptyList(),
)

fun MemoryHook.toShareCard() = ShareCardData(
    tag = "Memory Hook",
    word = word,
    pos = pos,
    ipa = "$ipa · $bnPronunciation",
    meaningBn = meaningBn,
    sections = listOf(
        "ধ্বনি-সূত্র" to "$soundSplit  →  $soundHookBn",
        "গল্প" to storyBn,
        "মূল সূত্র 🔑" to keyCueBn,
        "উদাহরণ" to "$exampleEn\n$exampleBn",
    ),
)

fun ConfusedPair.toShareCard() = ShareCardData(
    tag = "পার্থক্য কী?",
    word = "${a.word} vs ${b.word}",
    meaningBn = "${a.word}: ${a.meaningBn}\n${b.word}: ${b.meaningBn}",
    sections = listOfNotNull(
        "পার্থক্য" to differenceBn,
        mnemonic.takeIf { it.isNotBlank() }?.let { "মনে রাখুন 💡" to it },
        "উদাহরণ" to "${a.exampleEn}\n${b.exampleEn}",
    ),
)

fun StudyItem.toShareCard() = ShareCardData(
    tag = "${module.number}. ${module.titleEn}",
    word = title,
    pos = pos,
    ipa = subtitle,
    meaningBn = answer,
    sections = if (detail.isBlank()) emptyList() else listOf("বিস্তারিত" to detail),
)

/** Draws the card with the platform Canvas, so it needs no screen and works offline. */
object ShareCardRenderer {
    private const val W = 1080
    private const val PAD = 72f
    private val NAVY = 0xFF0D2C54.toInt()
    private val GOLD = 0xFFF2A900.toInt()
    private val INK = 0xFF1B1F27.toInt()
    private val MUTED = 0xFF5A6270.toInt()
    private val BG = 0xFFF8F9FC.toInt()
    private val CARD = 0xFFFFFFFF.toInt()
    private val LABEL = 0xFF8A6100.toInt()

    fun render(context: Context, d: ShareCardData): Bitmap {
        val regular = ResourcesCompat.getFont(context, R.font.hind_siliguri_regular) ?: Typeface.DEFAULT
        val bold = ResourcesCompat.getFont(context, R.font.hind_siliguri_bold) ?: Typeface.DEFAULT_BOLD
        val inner = (W - 2 * PAD - 2 * 40).toInt()

        fun paint(tf: Typeface, size: Float, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = tf; textSize = size; this.color = color }
        fun layout(text: String, p: TextPaint, width: Int = inner, maxLines: Int = 12) =
            StaticLayout.Builder.obtain(text, 0, text.length, p, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, 1.15f)
                .setMaxLines(maxLines).setEllipsize(android.text.TextUtils.TruncateAt.END).build()

        val posInfo = d.pos?.let { Pos.of(it) }
        val wordL = layout(d.word, paint(bold, if (d.word.length > 18) 72f else 92f, NAVY), maxLines = 3)
        val ipaL = d.ipa.takeIf { it.isNotBlank() }?.let { layout(it, paint(Typeface.DEFAULT, 36f, MUTED), maxLines = 2) }
        val meanL = layout(d.meaningBn, paint(bold, 52f, INK), maxLines = 4)
        val secs = d.sections.map { (label, text) ->
            layout(label, paint(bold, 32f, LABEL), maxLines = 1) to layout(text, paint(regular, 38f, INK), maxLines = 7)
        }

        val header = 190f
        val footer = 150f
        var body = 48f + wordL.height + 20f
        if (posInfo != null) body += 64f
        if (ipaL != null) body += ipaL.height + 16f
        body += meanL.height + 36f
        secs.forEach { (l, t) -> body += l.height + 6f + t.height + 28f }
        body += 24f
        val h = (header + 40f + body + 40f + footer).toInt().coerceAtMost(2400)

        val bmp = Bitmap.createBitmap(W, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(BG)

        // Header band
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        fill.color = NAVY
        c.drawRect(0f, 0f, W.toFloat(), header, fill)
        c.drawText("Vocabsolution", PAD, 100f, paint(bold, 60f, 0xFFFFFFFF.toInt()))
        c.drawText("by English Doctor", PAD, 152f, paint(bold, 34f, GOLD))
        val tagP = paint(bold, 30f, NAVY)
        val tagW = tagP.measureText(d.tag) + 48f
        fill.color = GOLD
        c.drawRoundRect(RectF(W - PAD - tagW, 70f, W - PAD, 124f), 27f, 27f, fill)
        c.drawText(d.tag, W - PAD - tagW + 24f, 108f, tagP)

        // Body card
        fill.color = CARD
        val top = header + 40f
        c.drawRoundRect(RectF(PAD, top, W - PAD, top + body), 36f, 36f, fill)
        fill.color = posInfo?.light?.toArgb() ?: NAVY
        c.drawRect(PAD, top + 36f, PAD + 10f, top + body - 36f, fill)

        var y = top + 48f
        val x = PAD + 40f
        fun draw(l: StaticLayout) { c.save(); c.translate(x, y); l.draw(c); c.restore(); y += l.height }

        draw(wordL); y += 20f
        if (posInfo != null) {
            val label = "${posInfo.symbol} ${posInfo.abbr} · ${posInfo.bn}"
            val pp = paint(bold, 30f, posInfo.light.toArgb())
            val pw = pp.measureText(label) + 40f
            fill.color = posInfo.light.copy(alpha = 0.14f).toArgb()
            c.drawRoundRect(RectF(x, y, x + pw, y + 48f), 24f, 24f, fill)
            c.drawText(label, x + 20f, y + 35f, pp)
            y += 64f
        }
        ipaL?.let { draw(it); y += 16f }
        draw(meanL); y += 36f
        secs.forEach { (l, t) -> draw(l); y += 6f; draw(t); y += 28f }

        // Footer
        val fy = h - footer
        fill.color = NAVY
        c.drawRect(0f, fy, W.toFloat(), h.toFloat(), fill)
        c.drawText("শাহজালাল সালতানাত (ইংলিশ ডক্টর)", PAD, fy + 64f, paint(bold, 34f, 0xFFFFFFFF.toInt()))
        c.drawText("মুখস্থ নয়, বুঝে শেখা · Vocabsolution অ্যাপ", PAD, fy + 112f, paint(regular, 30f, GOLD))
        return bmp
    }

    /** Saves the card in the app cache and opens the system share sheet. */
    fun share(context: Context, d: ShareCardData) {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() } // keep only the latest card
        val file = File(dir, "vocabsolution_${System.currentTimeMillis()}.png")
        file.outputStream().use { render(context, d).compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "${d.word} — ${d.meaningBn.lines().first()}\nVocabsolution by English Doctor দিয়ে শিখছি 📚")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "শেয়ার করুন").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
