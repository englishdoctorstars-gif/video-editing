package com.englishdoctor.vocabsolution.store

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.englishdoctor.vocabsolution.R
import com.englishdoctor.vocabsolution.ui.theme.BrandGold
import com.englishdoctor.vocabsolution.ui.theme.BrandNavy
import com.englishdoctor.vocabsolution.ui.theme.HindSiliguri
import com.englishdoctor.vocabsolution.ui.theme.WrongRed
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.math.exp

/*
 * "৫ সেকেন্ডের চ্যালেঞ্জ" — a psychology-led sales ad with no author/developer introduction.
 * The viewer is taught one word inside the ad (proof by experience), then: pain → cause →
 * forgetting curve vs spaced review → future self → effort → value stack + price anchor → CTA.
 * Square design at 540×540dp; the 9:16 render places it in the Reels safe zone.
 * Animated by tools/marketing/build_psych_ad.py.
 */

private fun tp(size: TextUnit, color: Color = Color.White, weight: FontWeight = FontWeight.Bold, align: TextAlign = TextAlign.Center) =
    TextStyle(fontFamily = HindSiliguri, fontSize = size, color = color, fontWeight = weight, textAlign = align, lineHeight = size * 1.25f)

private val Ink = Color(0xFF060A1A)
private val Deep = Color(0xFF0B1433)
private val Glass = Color(0xFF121A3E)
private val Gold2 = Color(0xFFFFD45C)
private val Teal = Color(0xFF26A69A)
private val Violet = Color(0xFFAB47BC)
private val Sky = Color(0xFF42A5F5)
private val Mint = Color(0xFF7BE0A0)
private val Soft = Color.White.copy(alpha = 0.82f)

@Composable private fun Full(content: @Composable BoxScope.() -> Unit) = Box(Modifier.fillMaxSize(), content = content)

@Composable
private fun At(x: Dp, y: Dp, content: @Composable () -> Unit) = Layout(content, Modifier.fillMaxSize()) { ms, c ->
    val p = ms.first().measure(c.copy(minWidth = 0, minHeight = 0))
    layout(c.maxWidth, c.maxHeight) { p.place(x.roundToPx() - p.width / 2, y.roundToPx() - p.height / 2) }
}

@Composable
private fun Backdrop(top: Color, bottom: Color, glow: Color, glow2: Color = BrandGold) = Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
    Box(Modifier.size(620.dp).offset((-220).dp, (-260).dp).background(Brush.radialGradient(listOf(glow.copy(alpha = 0.32f), Color.Transparent))))
    Box(Modifier.size(640.dp).offset(260.dp, 300.dp).background(Brush.radialGradient(listOf(glow2.copy(alpha = 0.16f), Color.Transparent))))
    Canvas(Modifier.fillMaxSize()) {
        for (i in 0 until 6) {
            val x = size.width * (0.1f + i * 0.18f)
            drawLine(Color.White.copy(alpha = 0.03f), Offset(x, 0f), Offset(x - size.height * 0.5f, size.height), 26f)
        }
        // Vignette keeps the eye on the centre.
        drawRect(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.38f)), radius = size.maxDimension * 0.75f))
    }
}

@Composable
private fun Particles() = Canvas(Modifier.fillMaxSize()) {
    val rnd = java.util.Random(23)
    repeat(60) {
        val x = rnd.nextFloat() * size.width
        val y = rnd.nextFloat() * size.height
        val gold = rnd.nextFloat() < 0.45f
        drawCircle((if (gold) BrandGold else Color.White).copy(alpha = 0.08f + rnd.nextFloat() * 0.22f), 1.2f + rnd.nextFloat() * 3f, Offset(x, y))
    }
}

@Composable
private fun Wipe() = Canvas(Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    val slant = h * 0.35f
    drawPath(Path().apply { moveTo(slant, 0f); lineTo(w, 0f); lineTo(w - slant, h); lineTo(0f, h); close() }, Brush.horizontalGradient(listOf(BrandNavy, Deep)))
    drawPath(Path().apply { moveTo(w - 40f, 0f); lineTo(w, 0f); lineTo(w - slant, h); lineTo(w - slant - 40f, h); close() }, Brush.verticalGradient(listOf(Gold2, BrandGold)))
}

@Composable
private fun Pill(text: String, size: TextUnit = 16.sp) = Text(
    text, style = tp(size, BrandNavy),
    modifier = Modifier.shadow(10.dp, RoundedCornerShape(50)).background(Brush.horizontalGradient(listOf(Gold2, BrandGold)), RoundedCornerShape(50)).padding(horizontal = 18.dp, vertical = 4.dp),
)

@Composable
private fun Card(modifier: Modifier = Modifier, border: Color = Color.White.copy(alpha = 0.22f), content: @Composable () -> Unit) =
    Box(modifier.shadow(18.dp, RoundedCornerShape(22.dp)).background(Brush.verticalGradient(listOf(Glass, Deep)), RoundedCornerShape(22.dp))
        .border(1.5.dp, border, RoundedCornerShape(22.dp)).padding(horizontal = 18.dp, vertical = 10.dp)) { content() }

private fun screen(name: String) = listOf("../marketing/ad-screens", "marketing/ad-screens").map { File(it, "$name.png") }.first { it.exists() }
    .readBytes().let { BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap() }

@Composable
private fun Phone(name: String, width: Dp, tilt: Float = 0f) = Box(
    Modifier.rotate(tilt).width(width).height(width * 2.222f + 14.dp).shadow(30.dp, RoundedCornerShape(width / 6.5f))
        .background(Color(0xFF0B0B0F), RoundedCornerShape(width / 6.5f)).border(2.dp, Color(0xFF4A5070), RoundedCornerShape(width / 6.5f)).padding(6.dp),
) { Image(screen(name), null, Modifier.fillMaxSize().clip(RoundedCornerShape(width / 7.8f)), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter) }

// ---- A · pattern interrupt + open question ----------------------------------------------------

@Composable private fun AStop() = Full { At(270.dp, 78.dp) { Text("✋ একটু থামুন!", style = tp(34.sp, Gold2)) } }
@Composable private fun AQuestion() = Full { At(270.dp, 150.dp) { Text("এই শব্দটার অর্থ জানেন?", style = tp(30.sp)) } }
@Composable private fun AWord() = Full {
    At(270.dp, 262.dp) {
        Box(Modifier.shadow(30.dp, RoundedCornerShape(28.dp)).background(Brush.verticalGradient(listOf(Color(0xFF1B2660), Deep)), RoundedCornerShape(28.dp))
            .border(2.dp, Gold2.copy(alpha = 0.7f), RoundedCornerShape(28.dp)).padding(horizontal = 34.dp, vertical = 10.dp)) {
            Text("Sagacious", style = tp(62.sp))
        }
    }
}
@Composable private fun ATimer(n: Int) = Full {
    At(270.dp, 430.dp) {
        Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val s = 9.dp.toPx()
                drawArc(Color.White.copy(alpha = 0.15f), 0f, 360f, false, Offset(s, s), Size(size.width - 2 * s, size.height - 2 * s), style = Stroke(s))
                drawArc(if (n == 1) WrongRed else Gold2, -90f, 120f * n, false, Offset(s, s), Size(size.width - 2 * s, size.height - 2 * s), style = Stroke(s, cap = StrokeCap.Round))
            }
            Text("০১২৩"[n].toString(), style = tp(46.sp, if (n == 1) WrongRed else Gold2))
        }
    }
}

// ---- B · teach the word (the viewer experiences the method) -----------------------------------

@Composable private fun BHead() = Full { At(270.dp, 62.dp) { Pill("না জানলেও সমস্যা নেই — ৫ সেকেন্ড দিন", 19.sp) } }
@Composable private fun BWord() = Full { At(270.dp, 138.dp) { Text("Sagacious", style = tp(50.sp)) } }
@Composable private fun BSplit() = Full {
    At(270.dp, 218.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Saga · cious", style = tp(28.sp, Soft))
            Text("→", style = tp(30.sp, Gold2))
            Text("সাগর-শেষ", style = tp(38.sp, Gold2))
        }
    }
}
@Composable private fun BStory() = Full {
    At(270.dp, 322.dp) {
        Card(Modifier.width(470.dp), border = Gold2.copy(alpha = 0.5f)) {
            Text("যে মাঝি সাগরের শুরু থেকে শেষ পর্যন্ত চেনে…", style = tp(25.sp, weight = FontWeight.SemiBold), modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}
@Composable private fun BMeaning() = Full {
    At(270.dp, 438.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("…সে-ই তো বিচক্ষণ!", style = tp(30.sp))
            Text("Sagacious = বিচক্ষণ ✓", style = tp(40.sp, Mint))
        }
    }
}

// ---- C · realisation --------------------------------------------------------------------------

@Composable private fun C1() = Full { At(270.dp, 130.dp) { Text("দেখলেন?", style = tp(44.sp)) } }
@Composable private fun C2() = Full { At(270.dp, 225.dp) { Text("একটা শব্দ শিখে ফেললেন।", style = tp(40.sp, Gold2)) } }
@Composable private fun C3() = Full { At(270.dp, 330.dp) { Text("মুখস্থ ছাড়াই। কয়েক সেকেন্ডে।", style = tp(32.sp)) } }
@Composable private fun C4() = Full { At(270.dp, 420.dp) { Text("এভাবেই শেখায় Vocabsolution", style = tp(24.sp, Soft, FontWeight.SemiBold)) } }

// ---- D · pain, then the real cause ------------------------------------------------------------

private val Pains = listOf("📖" to "রাত জেগে মুখস্থ… সকালে উধাও", "📝" to "পরীক্ষার হলে — মাথা ফাঁকা", "😓" to "চেনা শব্দ, অথচ অর্থ মনে পড়ে না")

@Composable private fun DHead() = Full { At(270.dp, 62.dp) { Text("অথচ এতদিন…", style = tp(36.sp)) } }
@Composable private fun DCard(i: Int) = Full {
    At(270.dp, (150 + i * 92).dp) {
        Row(Modifier.width(456.dp).shadow(14.dp, RoundedCornerShape(20.dp)).background(Color(0xFF2A0F1C), RoundedCornerShape(20.dp))
            .border(1.5.dp, WrongRed.copy(alpha = 0.7f), RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(Pains[i].first, style = tp(30.sp))
            Spacer(Modifier.width(12.dp))
            Text(Pains[i].second, style = tp(23.sp, align = TextAlign.Start))
        }
    }
}
@Composable private fun DTruth() = Full {
    At(270.dp, 462.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("সমস্যা আপনার মেধায় না —", style = tp(26.sp))
            Text("সমস্যা পদ্ধতিতে।", style = tp(40.sp, Gold2))
        }
    }
}

// ---- E · forgetting curve vs spaced review (illustrative) -------------------------------------

private const val CX0 = 70f
private const val CX1 = 485f
private const val CY0 = 150f   // remembered: all
private const val CY1 = 385f   // remembered: nothing
private val Reviews = listOf(0.10f to "১", 0.27f to "৩", 0.52f to "৭", 0.90f to "৩০")

private fun redY(x: Float) = 1f - (0.12f + 0.88f * exp(-5.5f * x))
private fun goldPoints(upTo: Float): List<Pair<Float, Float>> {
    val pts = mutableListOf(0f to 0f)
    val marks = listOf(0f) + Reviews.map { it.first } + listOf(1f)
    val drops = listOf(0.30f, 0.24f, 0.17f, 0.10f, 0.02f)
    for (i in 0 until marks.size - 1) {
        val a = marks[i]; val b = marks[i + 1]
        if (upTo <= a) break
        val end = minOf(upTo, b)
        pts += end to drops[i] * (end - a) / (b - a)
        if (upTo >= b && i < marks.size - 2) pts += b to 0f
    }
    return pts
}

@Composable private fun EHead() = Full { At(270.dp, 52.dp) { Text("কেন ভুলে যান — আর সমাধান কী", style = tp(30.sp)) } }
@Composable private fun EAxes() = Full {
    At(270.dp, 272.dp) {
        Box(Modifier.width(480.dp).height(330.dp).shadow(16.dp, RoundedCornerShape(24.dp)).background(Brush.verticalGradient(listOf(Glass, Deep)), RoundedCornerShape(24.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(24.dp)))
    }
    Canvas(Modifier.fillMaxSize()) {
        val d = density
        drawLine(Color.White.copy(alpha = 0.5f), Offset(CX0 * d, 135 * d), Offset(CX0 * d, CY1 * d), 2 * d)
        drawLine(Color.White.copy(alpha = 0.5f), Offset(CX0 * d, CY1 * d), Offset((CX1 + 10) * d, CY1 * d), 2 * d)
        for (k in 1..3) drawLine(Color.White.copy(alpha = 0.07f), Offset(CX0 * d, (CY0 + (CY1 - CY0) * k / 4) * d), Offset(CX1 * d, (CY0 + (CY1 - CY0) * k / 4) * d), 1 * d)
    }
    At(108.dp, 128.dp) { Text("মনে থাকা ↑", style = tp(13.sp, Soft, FontWeight.SemiBold)) }
    At(455.dp, 420.dp) { Text("দিন →", style = tp(13.sp, Soft, FontWeight.SemiBold)) }
    At(270.dp, 421.dp) { Text("(ধারণাগত চিত্র)", style = tp(11.sp, Color.White.copy(alpha = 0.5f), FontWeight.Normal)) }
}
@Composable private fun ERed(p: Float) = Full {
    Canvas(Modifier.fillMaxSize()) {
        val d = density
        val path = Path()
        val n = 60
        for (k in 0..(n * p).toInt()) {
            val x = k / n.toFloat()
            val px = (CX0 + (CX1 - CX0) * x) * d; val py = (CY0 + (CY1 - CY0) * redY(x)) * d
            if (k == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        drawPath(path, WrongRed, style = Stroke(5 * d, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    if (p >= 1f) At(410.dp, 338.dp) { Text("শুধু মুখস্থ", style = tp(17.sp, Color.White), modifier = Modifier.background(WrongRed, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 1.dp)) }
}
@Composable private fun EGold(p: Float) = Full {
    Canvas(Modifier.fillMaxSize()) {
        val d = density
        val path = Path()
        goldPoints(p).forEachIndexed { k, (x, y) ->
            val px = (CX0 + (CX1 - CX0) * x) * d; val py = (CY0 + (CY1 - CY0) * y) * d
            if (k == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        drawPath(path, Gold2, style = Stroke(5 * d, cap = StrokeCap.Round, join = StrokeJoin.Round))
        Reviews.filter { it.first <= p }.forEach { (x, _) ->
            val px = (CX0 + (CX1 - CX0) * x) * d
            drawLine(Gold2.copy(alpha = 0.35f), Offset(px, CY0 * d), Offset(px, CY1 * d), 1.5f * d)
            drawCircle(Gold2, 7 * d, Offset(px, CY0 * d))
        }
    }
    Reviews.filter { it.first <= p }.forEach { (x, label) ->
        At((CX0 + (CX1 - CX0) * x).dp, 402.dp) { Text(label, style = tp(15.sp, Gold2)) }
    }
    if (p >= 1f) At(380.dp, 190.dp) { Text("বুঝে শেখা + রিভিউ", style = tp(17.sp, BrandNavy), modifier = Modifier.background(Gold2, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 1.dp)) }
}
@Composable private fun ECaption() = Full {
    At(270.dp, 486.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("অ্যাপ নিজেই মনে করিয়ে দেয়", style = tp(24.sp))
            Text("১ · ৩ · ৭ · ৩০ দিনে রিভিউ", style = tp(22.sp, Gold2))
        }
    }
}

// ---- F · future self --------------------------------------------------------------------------

private val Futures = listOf("অচেনা শব্দ দেখেই ভেঙে অর্থ বুঝে ফেলছেন", "পরীক্ষার হলে শব্দ নিজেই মনে পড়ছে", "IELTS passage-এ paraphrase চোখে পড়ছে")

@Composable private fun FHead() = Full { At(270.dp, 66.dp) { Text("এবার কল্পনা করুন…", style = tp(36.sp)) } }
@Composable private fun FRow(i: Int) = Full {
    At(270.dp, (160 + i * 88).dp) {
        Row(Modifier.width(470.dp).shadow(14.dp, RoundedCornerShape(20.dp)).background(Color(0xFF0C2A22), RoundedCornerShape(20.dp))
            .border(1.5.dp, Mint.copy(alpha = 0.7f), RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).background(Mint, CircleShape), contentAlignment = Alignment.Center) { Text("✓", style = tp(22.sp, Color(0xFF06301F))) }
            Spacer(Modifier.width(12.dp))
            Text(Futures[i], style = tp(21.sp, align = TextAlign.Start))
        }
    }
}
@Composable private fun FTag() = Full {
    At(270.dp, 458.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(70.dp))
            Column {
                Text("Vocabsolution", style = tp(34.sp, align = TextAlign.Start))
                Text("মুখস্থ নয়, বুঝে শেখা", style = tp(19.sp, Gold2, align = TextAlign.Start))
            }
        }
    }
}

// ---- G · low effort, real app -----------------------------------------------------------------

@Composable private fun GHead() = Full {
    At(270.dp, 62.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("দিনে মাত্র ১০ মিনিট", style = tp(40.sp, Gold2))
            Text("বাসে · ক্লাসের ফাঁকে · ইন্টারনেট ছাড়াই", style = tp(19.sp, Soft, FontWeight.SemiBold))
        }
    }
}
@Composable private fun GPhone(i: Int) = Full {
    val (name, x, w) = listOf(Triple("06_word_breaker", 105, 124), Triple("05_hook_sagacious_story", 270, 150), Triple("14_exam_ready", 435, 124))[i]
    At(x.dp, (if (i == 1) 292 else 304).dp) { Phone(name, w.dp, listOf(-6f, 0f, 6f)[i]) }
}
@Composable private fun GLabels() = Full {
    listOf("ভেঙে বুঝুন" to 105, "গল্পে মনে রাখুন" to 270, "যাচাই করুন" to 435).forEach { (t, x) -> At(x.dp, 492.dp) { Pill(t, 17.sp) } }
}

// ---- H · value stack + price anchor -----------------------------------------------------------

private val Stack = listOf("২,৬৯৪ শব্দ — ৯৬টি শব্দ-পরিবারে", "১০০টি মনে রাখার গল্প", "১,১৫২ IELTS paraphrase · ২৫০ MCQ", "অফলাইনে চলে · কোনো বিজ্ঞাপন নেই")

@Composable private fun HHead() = Full { At(270.dp, 44.dp) { Text("এক অ্যাপে যা যা পাচ্ছেন", style = tp(30.sp)) } }
@Composable private fun HRow(i: Int) = Full {
    At(270.dp, (100 + i * 46).dp) {
        Row(Modifier.width(430.dp).background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)).border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("✓", style = tp(21.sp, Mint)); Spacer(Modifier.width(10.dp)); Text(Stack[i], style = tp(20.sp, align = TextAlign.Start))
        }
    }
}
@Composable private fun HOld() = Full { At(270.dp, 302.dp) { Text("বইয়ের মূল্য ৭৫০ টাকা", style = tp(26.sp, Soft, FontWeight.SemiBold)) } }
@Composable private fun HStrike() = Full {
    Canvas(Modifier.fillMaxSize()) { drawLine(WrongRed, Offset(135.dp.toPx(), 310.dp.toPx()), Offset(405.dp.toPx(), 294.dp.toPx()), 5.dp.toPx(), cap = StrokeCap.Round) }
}
@Composable private fun HPrice() = Full {
    At(270.dp, 392.dp) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("অ্যাপে মাত্র", style = tp(26.sp), modifier = Modifier.padding(bottom = 22.dp))
            Text(Sales.PRICE, style = tp(76.sp, Gold2))
        }
    }
}
@Composable private fun HSub() = Full { At(270.dp, 486.dp) { Pill("এক বেলার নাস্তার দামে — পুরো শব্দভান্ডার", 19.sp) } }

// ---- I · decision + call to action ------------------------------------------------------------

@Composable private fun ILine() = Full {
    At(270.dp, 60.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("আর কতদিন মুখস্থ করে ভুলবেন?", style = tp(27.sp))
            Text("আজই শুরু করুন।", style = tp(30.sp, Gold2))
        }
    }
}
@Composable private fun IButton() = Full {
    At(270.dp, 225.dp) {
        Column(Modifier.width(440.dp).shadow(26.dp, RoundedCornerShape(28.dp)).background(Brush.verticalGradient(listOf(Color(0xFF2FBF71), Color(0xFF1E9E57))), RoundedCornerShape(28.dp))
            .border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(28.dp)).padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💬 WhatsApp-এ লিখুন: VOCAB", style = tp(26.sp))
            Text(Sales.NUMBER, style = tp(44.sp))
        }
    }
}
@Composable private fun IPay() = Full {
    At(270.dp, 410.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Pill("পুরো অ্যাপ মাত্র ${Sales.PRICE}", 22.sp)
            Text("💳 bKash / Nagad: ${Sales.NUMBER}", style = tp(22.sp), modifier = Modifier.background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 5.dp))
            Text("👍 অথবা Facebook পেজ: ${Sales.PAGE}", style = tp(19.sp, Soft, FontWeight.SemiBold))
        }
    }
}

// ---- M · full feature tour (used by the long "সব এক অ্যাপে" ad) ---------------------------------

private class Feature(val q: String, val a: String, val screen: String, val accent: Color, val cards: List<String>)

private val Features = listOf(
    Feature("বড় শব্দ দেখলেই ভয়?", "ভাঙলেই অর্থ পরিষ্কার", "06_word_breaker", Teal,
        listOf("de + human + ize + ation", "= অমানবিকীকরণ", "উপসর্গ · মূল · প্রত্যয় চিনুন")),
    Feature("একটা একটা করে আর কত?", "১টি মূল → ১০১টি শব্দ", "07_root_family_act", Sky,
        listOf("ACT → action · active · react…", "৯৬টি শব্দ-পরিবার · ২,৬৯৪ শব্দ", "রঙ দেখেই চিনুন: ■ noun ▲ verb ● adjective")),
    Feature("affect না effect?", "আর গুলিয়ে ফেলবেন না", "08_confused_affect_effect", Violet,
        listOf("affect = প্রভাবিত করা (verb)", "effect = ফল, প্রভাব (noun)", "💡 A = Action, E = End result")),
    Feature("অজান্তেই ভুল ইংরেজি?", "ভুল ধরুন, ঠিকটা শিখুন", "09_common_mistakes", Color(0xFFFF8F00),
        listOf("✗ do a mistake", "✓ make a mistake", "✗ eat medicine → ✓ take medicine")),
    Feature("IELTS passage কঠিন লাগে?", "প্রসঙ্গ থেকে অর্থ ধরুন", "10_read_smart_passage", Mint,
        listOf("আন্দাজ মোড: আগে ভাবুন, পরে ট্যাপ", "১,১৫২টি paraphrase", "mitigate ≈ reduce the harm")),
    Feature("শুনে শব্দ ধরতে পারেন না?", "শুনুন, তারপর বানান লিখুন", "12_listen_and_spell", Sky,
        listOf("🔊 ব্রিটিশ উচ্চারণ + IPA", "🐢 ধীরে শোনার অপশন", "বানানের ফাঁদ চিনুন")),
    Feature("Writing-এ একই শব্দ বারবার?", "উচ্চ Band-এর শব্দ শিখুন", "13_write_graph_quiz", Violet,
        listOf("Task 1: গ্রাফ বর্ণনার শব্দ", "Task 2 ও Linking words", "increased gradually · fluctuated")),
    Feature("বিসিএস · ব্যাংক · চাকরির পরীক্ষা?", "ঘড়ি ধরে প্র্যাকটিস", "14_exam_ready", Gold2,
        listOf("২৫০টি MCQ", "⏱️ ২০ মিনিটের সেট", "বিসিএস · ব্যাংক · এনটিআরসিএ ধাঁচে")),
    Feature("শুরু করে ছেড়ে দেন?", "অ্যাপই আপনাকে ধরে রাখবে", "18_progress_badges", Color(0xFFFF8F00),
        listOf("🔥 Streak · ⭐ XP · 🏅 ব্যাজ", "📅 প্রতিদিন Word of the Day", "📱 হোম স্ক্রিন উইজেট · 🌙 ডার্ক মোড")),
)

private fun bnDigit(n: Int) = "০১২৩৪৫৬৭৮৯"[n].toString()

@Composable private fun MTitle(i: Int) = Full {
    val f = Features[i]
    Column(Modifier.align(Alignment.TopCenter).padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pill("${bnDigit(i + 1)} / ${bnDigit(Features.size)}", 14.sp)
            Text(f.q, style = tp(21.sp, Soft, FontWeight.SemiBold))
        }
        Spacer(Modifier.height(4.dp))
        Text(f.a, style = tp(35.sp, f.accent))
    }
}
@Composable private fun MPhone(i: Int) = Full { At((if (i % 2 == 0) 112 else 428).dp, 322.dp) { Phone(Features[i].screen, 150.dp, if (i % 2 == 0) -3f else 3f) } }
@Composable private fun MCard(i: Int, j: Int) = Full {
    val f = Features[i]
    At((if (i % 2 == 0) 372 else 168).dp, (196 + j * 104).dp) {
        Box(Modifier.width(292.dp).height(88.dp).shadow(16.dp, RoundedCornerShape(20.dp)).background(Brush.verticalGradient(listOf(Glass, Deep)), RoundedCornerShape(20.dp))
            .border(1.5.dp, f.accent.copy(alpha = 0.65f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(f.cards[j], style = tp(if (f.cards[j].length > 24) 18.sp else 22.sp, if (j == 1) f.accent else Color.White))
        }
    }
}
@Composable private fun C5() = Full {
    At(270.dp, 425.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("এমন ১০০টি গল্প আছে অ্যাপে", style = tp(24.sp, Soft, FontWeight.SemiBold))
            Pill("আর আছে আরও ৯টি শক্তি — শেষ পর্যন্ত দেখুন", 18.sp)
        }
    }
}

// ---- layer list -------------------------------------------------------------------------------

private const val CURVE_STEPS = 10

private val PSYCH_LAYERS: List<Pair<String, @Composable () -> Unit>> = buildList {
    add("a_stop" to { AStop() }); add("a_q" to { AQuestion() }); add("a_word" to { AWord() })
    (1..3).forEach { n -> add("a_t$n" to { ATimer(n) }) }
    add("b_head" to { BHead() }); add("b_word" to { BWord() }); add("b_split" to { BSplit() }); add("b_story" to { BStory() }); add("b_mean" to { BMeaning() })
    add("c_1" to { C1() }); add("c_2" to { C2() }); add("c_3" to { C3() }); add("c_4" to { C4() })
    add("d_head" to { DHead() }); (0..2).forEach { i -> add("d_card$i" to { DCard(i) }) }; add("d_truth" to { DTruth() })
    add("e_head" to { EHead() }); add("e_axes" to { EAxes() })
    (1..CURVE_STEPS).forEach { k -> add("e_red%02d".format(k) to { ERed(k / CURVE_STEPS.toFloat()) }) }
    (1..CURVE_STEPS).forEach { k -> add("e_gold%02d".format(k) to { EGold(k / CURVE_STEPS.toFloat()) }) }
    add("e_cap" to { ECaption() })
    add("f_head" to { FHead() }); (0..2).forEach { i -> add("f_row$i" to { FRow(i) }) }; add("f_tag" to { FTag() })
    add("g_head" to { GHead() }); (0..2).forEach { i -> add("g_ph$i" to { GPhone(i) }) }; add("g_lab" to { GLabels() })
    add("h_head" to { HHead() }); (0..3).forEach { i -> add("h_row$i" to { HRow(i) }) }
    add("h_old" to { HOld() }); add("h_strike" to { HStrike() }); add("h_price" to { HPrice() }); add("h_sub" to { HSub() })
    add("i_line" to { ILine() }); add("i_btn" to { IButton() }); add("i_pay" to { IPay() })
    add("c_5" to { C5() })
    Features.indices.forEach { i ->
        add("m${i}_title" to { MTitle(i) }); add("m${i}_phone" to { MPhone(i) })
        (0..2).forEach { j -> add("m${i}_c$j" to { MCard(i, j) }) }
    }
}

private val PSYCH_BACKGROUNDS: List<Pair<String, @Composable () -> Unit>> = listOf(
    "bg_ink" to { Backdrop(Ink, Color(0xFF151036), Color(0xFF6C4DFF)) },
    "bg_navy" to { Backdrop(Color(0xFF071A3A), Color(0xFF123A73), Sky) },
    "bg_teal" to { Backdrop(Color(0xFF041E26), Color(0xFF0B4452), Teal) },
    "bg_plum" to { Backdrop(Color(0xFF150A2A), Color(0xFF34195A), Violet) },
    "bg_pain" to { Backdrop(Color(0xFF14060C), Color(0xFF3A0E1C), WrongRed, WrongRed) },
)

class PsychAdLayersTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1080, xdpi = 320, ydpi = 320, density = Density.XHIGH),
        theme = "android:Theme.Translucent.NoTitleBar",
        useDeviceResolution = true,
    )

    @Test
    fun square() {
        PSYCH_BACKGROUNDS.forEach { (n, c) -> paparazzi.snapshot(name = n) { c() } }
        PSYCH_LAYERS.forEach { (n, c) -> paparazzi.snapshot(name = n) { c() } }
        paparazzi.snapshot(name = "particles") { Particles() }
        paparazzi.snapshot(name = "wipe") { Wipe() }
    }
}

@Composable
private fun SafeZone(content: @Composable () -> Unit) = Box(Modifier.fillMaxSize()) {
    Box(Modifier.offset(y = 113.dp).size(540.dp).graphicsLayer(scaleX = 0.9f, scaleY = 0.9f)) { content() }
}

@Composable private fun BrandLine() = Full {
    Row(Modifier.align(Alignment.TopCenter).padding(top = 72.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(40.dp))
        Text("Vocabsolution · মুখস্থ নয়, বুঝে শেখা", style = tp(17.sp, Color.White.copy(alpha = 0.8f), FontWeight.SemiBold))
    }
}

class PsychAdVerticalLayersTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1920, xdpi = 320, ydpi = 320, density = Density.XHIGH),
        theme = "android:Theme.Translucent.NoTitleBar",
        useDeviceResolution = true,
    )

    @Test
    fun vertical() {
        PSYCH_BACKGROUNDS.forEach { (n, c) -> paparazzi.snapshot(name = n) { c() } }
        PSYCH_LAYERS.forEach { (n, c) -> paparazzi.snapshot(name = n) { SafeZone(c) } }
        paparazzi.snapshot(name = "particles") { Particles() }
        paparazzi.snapshot(name = "wipe") { Wipe() }
        paparazzi.snapshot(name = "brand_line") { BrandLine() }
    }
}
