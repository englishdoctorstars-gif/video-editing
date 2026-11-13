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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
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
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.HindSiliguri
import com.englishdoctor.vocabsolution.ui.theme.Pos
import com.englishdoctor.vocabsolution.ui.theme.WrongRed
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

/*
 * "Vocabsolution Pro" sales ad: hook → reframe → four methods → proof → author → offer → CTA.
 * Square design at 540×540dp (1080px, xhdpi); the 9:16 render places it in the Reels safe zone.
 * Animated by tools/marketing/build_pro_ad.py (slides, staggered pops, wipes, count-up, pulse).
 */

private fun tp(size: TextUnit, color: Color = Color.White, weight: FontWeight = FontWeight.Bold, align: TextAlign = TextAlign.Center) =
    TextStyle(fontFamily = HindSiliguri, fontSize = size, color = color, fontWeight = weight, textAlign = align, lineHeight = size * 1.25f)

private val Ink = Color(0xFF060A1A)
private val Deep = Color(0xFF0B1433)
private val Glass = Color(0xFF121A3E)
private val Gold2 = Color(0xFFFFD45C)
private val Teal = Color(0xFF26A69A)
private val Orange = Color(0xFFFF8F00)
private val Violet = Color(0xFFAB47BC)
private val Sky = Color(0xFF42A5F5)

@Composable private fun Full(content: @Composable BoxScope.() -> Unit) = Box(Modifier.fillMaxSize(), content = content)

@Composable
private fun At(x: Dp, y: Dp, content: @Composable () -> Unit) = Layout(content, Modifier.fillMaxSize()) { ms, c ->
    val p = ms.first().measure(c.copy(minWidth = 0, minHeight = 0))
    layout(c.maxWidth, c.maxHeight) { p.place(x.roundToPx() - p.width / 2, y.roundToPx() - p.height / 2) }
}

@Composable
private fun Backdrop(top: Color, bottom: Color, glow: Color) = Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
    Box(Modifier.size(620.dp).offset((-220).dp, (-260).dp).background(Brush.radialGradient(listOf(glow.copy(alpha = 0.30f), Color.Transparent))))
    Box(Modifier.size(640.dp).offset(260.dp, 300.dp).background(Brush.radialGradient(listOf(BrandGold.copy(alpha = 0.16f), Color.Transparent))))
    // Thin diagonal light streaks give a premium, cinematic feel.
    Canvas(Modifier.fillMaxSize()) {
        for (i in 0 until 6) {
            val x = size.width * (0.1f + i * 0.18f)
            drawLine(Color.White.copy(alpha = 0.035f), Offset(x, 0f), Offset(x - size.height * 0.5f, size.height), 26f)
        }
    }
}

/** Floating light particles; the build script scrolls this layer upward slowly. */
@Composable
private fun Particles() = Canvas(Modifier.fillMaxSize()) {
    val rnd = java.util.Random(11)
    repeat(70) {
        val x = rnd.nextFloat() * size.width
        val y = rnd.nextFloat() * size.height
        val r = 1.2f + rnd.nextFloat() * 3.2f
        val gold = rnd.nextFloat() < 0.4f
        drawCircle((if (gold) BrandGold else Color.White).copy(alpha = 0.10f + rnd.nextFloat() * 0.25f), r, Offset(x, y))
    }
}

/** Scene transition: a slanted navy panel with a gold leading edge, swept across the frame. */
@Composable
private fun Wipe() = Canvas(Modifier.fillMaxSize()) {
    val w = size.width
    val h = size.height
    val slant = h * 0.35f
    val body = Path().apply { moveTo(slant, 0f); lineTo(w, 0f); lineTo(w - slant, h); lineTo(0f, h); close() }
    drawPath(body, Brush.horizontalGradient(listOf(BrandNavy, Deep)))
    val edge = Path().apply { moveTo(w - 40f, 0f); lineTo(w, 0f); lineTo(w - slant, h); lineTo(w - slant - 40f, h); close() }
    drawPath(edge, Brush.verticalGradient(listOf(Gold2, BrandGold)))
}

@Composable
private fun Kicker(text: String, modifier: Modifier = Modifier) = Text(
    text, style = tp(15.sp, BrandNavy), modifier = modifier.background(Brush.horizontalGradient(listOf(Gold2, BrandGold)), RoundedCornerShape(50))
        .padding(horizontal = 16.dp, vertical = 4.dp),
)

@Composable
private fun Title(kicker: String, title: String) = Full {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Kicker(kicker)
        Spacer(Modifier.height(8.dp))
        Text(title, style = tp(38.sp))
    }
}

@Composable
private fun GlassBox(modifier: Modifier = Modifier, border: Color = Color.White.copy(alpha = 0.22f), content: @Composable () -> Unit) =
    Box(modifier.shadow(18.dp, RoundedCornerShape(22.dp)).background(Brush.verticalGradient(listOf(Glass, Deep)), RoundedCornerShape(22.dp))
        .border(1.5.dp, border, RoundedCornerShape(22.dp)).padding(horizontal = 18.dp, vertical = 12.dp)) { content() }

private fun screen(name: String) = listOf("../marketing/ad-screens", "marketing/ad-screens").map { File(it, "$name.png") }.first { it.exists() }
    .readBytes().let { BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap() }

@Composable
private fun Phone(name: String, width: Dp) = Box(
    Modifier.width(width).height(width * 2.222f + 14.dp).shadow(30.dp, RoundedCornerShape(width / 6.5f))
        .background(Color(0xFF0B0B0F), RoundedCornerShape(width / 6.5f)).border(2.dp, Color(0xFF4A5070), RoundedCornerShape(width / 6.5f)).padding(7.dp),
) { Image(screen(name), null, Modifier.fillMaxSize().clip(RoundedCornerShape(width / 7.8f)), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter) }

// ---- 1 · hook ---------------------------------------------------------------------------------

private val Cloud = listOf(
    Triple("Abundant", 90f, -10f), Triple("Mitigate", 270f, 6f), Triple("Sagacious", 440f, -8f),
    Triple("Sabbatical", 150f, 8f), Triple("Exacerbate", 380f, -5f), Triple("Resilience", 260f, -3f),
)
private val CloudY = listOf(70f, 58f, 80f, 150f, 160f, 238f)

@Composable private fun HookCloud() = Full {
    Cloud.forEachIndexed { i, (w, x, r) ->
        At(x.dp, CloudY[i].dp) {
            Text(w, style = tp(24.sp), modifier = Modifier.rotate(r).alpha(0.9f).shadow(8.dp, RoundedCornerShape(12.dp))
                .background(Glass, RoundedCornerShape(12.dp)).border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp))
        }
    }
}
@Composable private fun HookLine1() = Full { At(270.dp, 330.dp) { Text("ইংরেজি শব্দ মুখস্থ করেন…", style = tp(36.sp)) } }
@Composable private fun HookLine2() = Full { At(270.dp, 400.dp) { Text("…আর পরের দিন ভুলে যান?", style = tp(36.sp, Gold2)) } }
@Composable private fun HookStrike() = Full {
    Canvas(Modifier.fillMaxSize()) {
        drawLine(WrongRed, Offset(40.dp.toPx(), 260.dp.toPx()), Offset(500.dp.toPx(), 40.dp.toPx()), 10.dp.toPx())
    }
}

// ---- 2 · reframe ------------------------------------------------------------------------------

@Composable private fun Reframe1() = Full { At(270.dp, 96.dp) { Text("দোষ আপনার না,", style = tp(40.sp)) } }
@Composable private fun Reframe2() = Full { At(270.dp, 160.dp) { Text("দোষ পদ্ধতির।", style = tp(54.sp, Gold2)) } }
@Composable private fun ReframeLogo() = Full {
    At(270.dp, 352.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(130.dp).shadow(30.dp, CircleShape).background(BrandNavy, CircleShape).border(3.dp, BrandGold, CircleShape),
                contentAlignment = Alignment.Center) { Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(130.dp)) }
            Spacer(Modifier.height(8.dp))
            Text("Vocabsolution", style = tp(38.sp))
            Text("মুখস্থ নয়, বুঝে শেখা", style = tp(22.sp, Gold2))
        }
    }
}

// ---- 3a · dissection -------------------------------------------------------------------------

private val Parts = listOf(Triple("de", "উপসর্গ", Teal), Triple("human", "মূল", Sky), Triple("ize", "প্রত্যয়", Orange), Triple("ation", "প্রত্যয়", Violet))

@Composable private fun DissWord() = Full { At(270.dp, 160.dp) { Text("Dehumanization", style = tp(52.sp)) } }

/** All four chips are laid out together; only [show] is visible, so staggered layers line up exactly. */
@Composable private fun DissChip(show: Int) = Full {
    At(270.dp, 262.dp) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Parts.forEachIndexed { i, (part, label, c) ->
                if (i > 0) Text("+", style = tp(30.sp, Color.White.copy(alpha = 0.7f)), modifier = Modifier.padding(top = 6.dp).alpha(if (i == show) 1f else 0f))
                Column(Modifier.alpha(if (i == show) 1f else 0f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(part, style = tp(32.sp), modifier = Modifier.shadow(10.dp, RoundedCornerShape(14.dp)).background(c, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 2.dp))
                    Text(label, style = tp(15.sp, c), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
@Composable private fun DissMeaning() = Full {
    At(270.dp, 380.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("= অমানবিকীকরণ", style = tp(40.sp, Gold2))
            Text("শব্দ ভাঙলেই অর্থ পরিষ্কার", style = tp(20.sp, Color.White.copy(alpha = 0.85f), FontWeight.SemiBold))
        }
    }
}

// ---- 3b · root power -------------------------------------------------------------------------

private val Ring = listOf("action" to "noun", "active" to "adjective", "react" to "verb", "actually" to "adverb",
    "activity" to "noun", "inactive" to "adjective", "activate" to "verb", "interact" to "verb")
private fun ringAt(i: Int): Pair<Float, Float> {
    val a = Math.toRadians(-90.0 + i * 45.0)
    return (270 + 200 * cos(a)).toFloat() to (290 + 130 * sin(a)).toFloat()
}
@Composable private fun RootCore() = Full {
    Canvas(Modifier.fillMaxSize()) {
        Ring.indices.forEach { i ->
            val (x, y) = ringAt(i)
            drawLine(Color.White.copy(alpha = 0.22f), Offset(270 * density, 290 * density), Offset(x * density, y * density), 2.dp.toPx())
        }
    }
    At(270.dp, 290.dp) {
        Box(Modifier.size(100.dp).shadow(26.dp, CircleShape).background(Brush.radialGradient(listOf(Gold2, BrandGold)), CircleShape),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("ACT", style = tp(32.sp, BrandNavy)); Text("মূল", style = tp(14.sp, BrandNavy)) }
        }
    }
}
@Composable private fun RootPair(pair: Int) = Full {
    listOf(pair, pair + 4).forEach { i ->
        val (w, pos) = Ring[i]
        val p = Pos.of(pos)
        val (x, y) = ringAt(i)
        At(x.dp, y.dp) {
            Text("${p.symbol} $w", style = tp(20.sp), modifier = Modifier.shadow(8.dp, RoundedCornerShape(50)).background(p.light, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 3.dp))
        }
    }
}
@Composable private fun RootBadge() = Full {
    At(270.dp, 488.dp) {
        Text("১টি মূল  →  ১০১টি শব্দ", style = tp(24.sp, BrandNavy), modifier = Modifier.shadow(12.dp, RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(Gold2, BrandGold)), RoundedCornerShape(50)).padding(horizontal = 22.dp, vertical = 6.dp))
    }
}

// ---- 3c · stories ----------------------------------------------------------------------------

@Composable private fun StoryPhone() = Full { At(130.dp, 318.dp) { Phone("05_hook_sagacious_story", 172.dp) } }
@Composable private fun StoryCard(step: Int) = Full {
    val (label, text, c) = listOf(Triple("কঠিন শব্দ", "Sagacious", Color.White), Triple("ধ্বনি-সূত্র", "সাগর-শেষ", Gold2),
        Triple("মনে রাখার গল্প → অর্থ", "= বিচক্ষণ ✓", Color(0xFF81C784)))[step]
    GlassBox(Modifier.offset(x = 250.dp, y = (130 + step * 112).dp).width(262.dp), border = c.copy(alpha = 0.6f)) {
        Column {
            Text(label, style = tp(14.sp, Color.White.copy(alpha = 0.65f), FontWeight.SemiBold, TextAlign.Start))
            Text(text, style = tp(30.sp, c, align = TextAlign.Start))
        }
    }
}

// ---- 3d · exams ------------------------------------------------------------------------------

@Composable private fun ExamPhone() = Full { At(410.dp, 318.dp) { Phone("14_exam_ready", 172.dp) } }
@Composable private fun ExamCard(step: Int) = Full {
    GlassBox(Modifier.offset(x = 28.dp, y = (138 + step * 170).dp).width(282.dp), border = if (step == 0) Color(0xFF81C784) else Gold2) {
        if (step == 0) Column {
            Text("IELTS Paraphrase", style = tp(16.sp, Color(0xFF81C784), align = TextAlign.Start))
            Text("reduce the harm", style = tp(22.sp, align = TextAlign.Start))
            Text("≈ mitigate ✓", style = tp(28.sp, Color(0xFF81C784), align = TextAlign.Start))
            Text("১,১৫২টি paraphrase", style = tp(14.sp, Color.White.copy(alpha = 0.7f), FontWeight.SemiBold, TextAlign.Start))
        } else Column {
            Text("চাকরির পরীক্ষা", style = tp(16.sp, Gold2, align = TextAlign.Start))
            Text("⏱️ ২০ মিনিটের MCQ সেট", style = tp(22.sp, align = TextAlign.Start))
            Text("বিসিএস · ব্যাংক · এনটিআরসিএ ধাঁচে ২৫০টি প্রশ্ন", style = tp(14.sp, Color.White.copy(alpha = 0.7f), FontWeight.SemiBold, TextAlign.Start))
        }
    }
}

// ---- 4 · proof (count-up) --------------------------------------------------------------------

private val Stats = listOf(2694 to "শব্দ, ৯৬টি শব্দ-পরিবারে", 1152 to "IELTS paraphrase", 250 to "চাকরির পরীক্ষার MCQ", 100 to "মনে রাখার গল্প")
private fun bn(n: Int): String {
    val d = "০১২৩৪৫৬৭৮৯"
    val s = n.toString().map { d[it - '0'] }.joinToString("")
    return if (n >= 1000) s.dropLast(3) + "," + s.takeLast(3) else s
}
private fun statPos(i: Int) = (148 + (i % 2) * 244) to (190 + (i / 2) * 160)

@Composable private fun StatsTitle() = Full {
    At(270.dp, 70.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("বইয়ের ১১টি অধ্যায়", style = tp(34.sp))
            Text("পুরোটাই এখন অ্যাপে", style = tp(26.sp, Gold2))
        }
    }
}
@Composable private fun StatsTiles() = Full {
    Stats.indices.forEach { i ->
        val (x, y) = statPos(i)
        At(x.dp, y.dp) {
            Box(Modifier.width(226.dp).height(140.dp).shadow(14.dp, RoundedCornerShape(22.dp)).background(Brush.verticalGradient(listOf(Glass, Deep)), RoundedCornerShape(22.dp))
                .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp)))
        }
        At(x.dp, (y + 36).dp) { Text(Stats[i].second, style = tp(16.sp, Color.White.copy(alpha = 0.8f), FontWeight.SemiBold)) }
    }
}
/** One frame of the count-up: every number at [fraction] of its final value. */
@Composable private fun StatsNumbers(fraction: Float) = Full {
    Stats.forEachIndexed { i, (n, _) ->
        val (x, y) = statPos(i)
        At(x.dp, (y - 14).dp) { Text(bn((n * fraction).toInt()) + if (fraction >= 1f) "" else "", style = tp(46.sp, Gold2)) }
    }
}

// ---- 5 · authority ---------------------------------------------------------------------------

@Composable private fun Author() = Full {
    At(270.dp, 280.dp) {
        Column(Modifier.width(470.dp).shadow(20.dp, RoundedCornerShape(26.dp)).background(Brush.verticalGradient(listOf(Glass, Deep)), RoundedCornerShape(26.dp))
            .border(1.5.dp, BrandGold.copy(alpha = 0.6f), RoundedCornerShape(26.dp)).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("যাঁর পদ্ধতি", style = tp(16.sp, Gold2))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.size(86.dp).background(Brush.radialGradient(listOf(Gold2, BrandGold)), CircleShape), contentAlignment = Alignment.Center) {
                Text("ED", style = tp(34.sp, BrandNavy))
            }
            Spacer(Modifier.height(8.dp))
            Text("শাহজালাল সালতানাত", style = tp(32.sp))
            Text("(ইংলিশ ডক্টর)", style = tp(22.sp, Gold2))
            Spacer(Modifier.height(8.dp))
            Text("ঢাকা বিশ্ববিদ্যালয় থেকে ইংরেজিতে স্নাতকোত্তর", style = tp(17.sp, Color.White.copy(alpha = 0.9f), FontWeight.SemiBold))
            Text("Corporate Trainer · Teacher Trainer · Gemini Certified Educator", style = tp(14.sp, Color.White.copy(alpha = 0.75f), FontWeight.SemiBold))
            Spacer(Modifier.height(6.dp))
            Text("তাঁর বই VOCABSOLUTION-এর পুরো পদ্ধতি — এখন আপনার ফোনে", style = tp(16.sp, Color.White, FontWeight.SemiBold))
        }
    }
}

// ---- 6 · offer -------------------------------------------------------------------------------

@Composable private fun OfferAnchor() = Full {
    At(270.dp, 96.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("বইয়ের মূল্য ৭৫০ টাকা", style = tp(24.sp, Color.White.copy(alpha = 0.8f), FontWeight.SemiBold))
            Text("তার পুরো কন্টেন্ট, আরও সহজে —", style = tp(24.sp))
        }
    }
}
@Composable private fun OfferPrice() = Full {
    At(270.dp, 250.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("অ্যাপে মাত্র", style = tp(28.sp, Color.White))
            Text(Sales.PRICE, style = tp(84.sp, Gold2), modifier = Modifier.graphicsLayer { shadowElevation = 30f })
        }
    }
}
@Composable private fun OfferPoints() = Full {
    At(270.dp, 430.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("✓ ইন্টারনেট ছাড়াই", "✓ বিজ্ঞাপন নেই", "✓ Android ফোনে").forEach {
                Text(it, style = tp(17.sp), modifier = Modifier.background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(50))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}

// ---- 7 · call to action ----------------------------------------------------------------------

@Composable private fun CtaTop() = Full {
    At(270.dp, 92.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(64.dp))
                Text("Vocabsolution", style = tp(34.sp))
            }
            Text("পুরো অ্যাপ মাত্র ${Sales.PRICE}", style = tp(24.sp, BrandNavy), modifier = Modifier.padding(top = 6.dp)
                .background(Brush.horizontalGradient(listOf(Gold2, BrandGold)), RoundedCornerShape(50)).padding(horizontal = 20.dp, vertical = 4.dp))
        }
    }
}
/** The big button pulses in the video. */
@Composable private fun CtaButton() = Full {
    At(270.dp, 262.dp) {
        Column(Modifier.width(420.dp).shadow(24.dp, RoundedCornerShape(28.dp)).background(Brush.verticalGradient(listOf(Color(0xFF2FBF71), Color(0xFF1E9E57))), RoundedCornerShape(28.dp))
            .padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💬 এখনই WhatsApp করুন", style = tp(26.sp))
            Text(Sales.NUMBER, style = tp(42.sp))
        }
    }
}
@Composable private fun CtaPay() = Full {
    At(270.dp, 410.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("💳 bKash / Nagad: ${Sales.NUMBER}", style = tp(22.sp), modifier = Modifier.background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp))
            Text("👍 অথবা Facebook পেজ: ${Sales.PAGE}", style = tp(20.sp, Color.White.copy(alpha = 0.9f), FontWeight.SemiBold))
        }
    }
}

// ---- shared layer list -----------------------------------------------------------------------

private val PRO_LAYERS: List<Pair<String, @Composable () -> Unit>> = buildList {
    add("hook_cloud" to { HookCloud() }); add("hook_1" to { HookLine1() }); add("hook_2" to { HookLine2() }); add("hook_strike" to { HookStrike() })
    add("re_1" to { Reframe1() }); add("re_2" to { Reframe2() }); add("re_logo" to { ReframeLogo() })
    add("m1_title" to { Title("পদ্ধতি ১ / ৪", "ভেঙে বুঝুন") }); add("m1_word" to { DissWord() })
    (0..3).forEach { i -> add("m1_chip$i" to { DissChip(i) }) }; add("m1_meaning" to { DissMeaning() })
    add("m2_title" to { Title("পদ্ধতি ২ / ৪", "এক মূলে অনেক শব্দ") }); add("m2_core" to { RootCore() })
    (0..3).forEach { i -> add("m2_pair$i" to { RootPair(i) }) }; add("m2_badge" to { RootBadge() })
    add("m3_title" to { Title("পদ্ধতি ৩ / ৪", "গল্পে মনে রাখুন") }); add("m3_phone" to { StoryPhone() })
    (0..2).forEach { i -> add("m3_card$i" to { StoryCard(i) }) }
    add("m4_title" to { Title("পদ্ধতি ৪ / ৪", "পরীক্ষায় এগিয়ে থাকুন") }); add("m4_phone" to { ExamPhone() })
    (0..1).forEach { i -> add("m4_card$i" to { ExamCard(i) }) }
    add("st_title" to { StatsTitle() }); add("st_tiles" to { StatsTiles() })
    (1..12).forEach { k -> add("st_num%02d".format(k) to { StatsNumbers(k / 12f) }) }
    add("au_card" to { Author() })
    add("of_anchor" to { OfferAnchor() }); add("of_price" to { OfferPrice() }); add("of_points" to { OfferPoints() })
    add("cta_top" to { CtaTop() }); add("cta_button" to { CtaButton() }); add("cta_pay" to { CtaPay() })
}

private val PRO_BACKGROUNDS: List<Pair<String, @Composable () -> Unit>> = listOf(
    "bg_ink" to { Backdrop(Ink, Color(0xFF151036), Color(0xFF6C4DFF)) },
    "bg_navy" to { Backdrop(Color(0xFF071A3A), Color(0xFF123A73), Sky) },
    "bg_teal" to { Backdrop(Color(0xFF041E26), Color(0xFF0B4452), Teal) },
    "bg_plum" to { Backdrop(Color(0xFF150A2A), Color(0xFF34195A), Violet) },
)

class ProAdLayersTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1080, xdpi = 320, ydpi = 320, density = Density.XHIGH),
        theme = "android:Theme.Translucent.NoTitleBar",
        useDeviceResolution = true,
    )

    @Test
    fun square() {
        PRO_BACKGROUNDS.forEach { (n, c) -> paparazzi.snapshot(name = n) { c() } }
        PRO_LAYERS.forEach { (n, c) -> paparazzi.snapshot(name = n) { c() } }
        paparazzi.snapshot(name = "particles") { Particles() }
        paparazzi.snapshot(name = "wipe") { Wipe() }
    }
}

// 9:16: the square design scaled into the Reels safe zone (see ComboLayersTest for the reasoning).
@Composable
private fun SafeZone(content: @Composable () -> Unit) = Box(Modifier.fillMaxSize()) {
    Box(Modifier.offset(y = 113.dp).size(540.dp).graphicsLayer(scaleX = 0.9f, scaleY = 0.9f)) { content() }
}

@Composable private fun BrandLine() = Full {
    Row(Modifier.align(Alignment.TopCenter).padding(top = 72.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(40.dp))
        Text("Vocabsolution · English Doctor", style = tp(17.sp, Color.White.copy(alpha = 0.8f), FontWeight.SemiBold))
    }
}

class ProAdVerticalLayersTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1920, xdpi = 320, ydpi = 320, density = Density.XHIGH),
        theme = "android:Theme.Translucent.NoTitleBar",
        useDeviceResolution = true,
    )

    @Test
    fun vertical() {
        PRO_BACKGROUNDS.forEach { (n, c) -> paparazzi.snapshot(name = n) { c() } }
        PRO_LAYERS.forEach { (n, c) -> paparazzi.snapshot(name = n) { SafeZone(c) } }
        paparazzi.snapshot(name = "particles") { Particles() }
        paparazzi.snapshot(name = "wipe") { Wipe() }
        paparazzi.snapshot(name = "brand_line") { BrandLine() }
    }
}
