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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
import com.englishdoctor.vocabsolution.ui.theme.WrongRed
import org.junit.Rule
import org.junit.Test
import java.io.File

/*
 * Concept 3 "স্বপ্নের Band" (IELTS) as layered motion graphics, 1080×1920 (360×640dp).
 * Backgrounds are opaque; every other layer is a transparent full-size PNG that
 * tools/marketing/build_concept3.py slides, fades and pops into place with ffmpeg.
 * Coordinates used for motion in that script assume this 360×640dp layout.
 */

private fun tx(size: TextUnit, color: Color = Color.White, weight: FontWeight = FontWeight.Bold, align: TextAlign = TextAlign.Center) =
    TextStyle(fontFamily = HindSiliguri, fontSize = size, color = color, fontWeight = weight, textAlign = align, lineHeight = size * 1.3f)

private val Night1 = Color(0xFF070B1E)
private val Night2 = Color(0xFF1B1F4B)
private val Glow = Color(0xFF6C7BFF)

@Composable
private fun Layer(content: @Composable BoxScope.() -> Unit) = Box(Modifier.fillMaxSize(), content = content)

@Composable
private fun Background(top: Color, bottom: Color, glow: Color, glow2: Color) = Box(
    Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom))),
) {
    Box(Modifier.size(420.dp).offset((-120).dp, (-80).dp).background(Brush.radialGradient(listOf(glow.copy(alpha = 0.35f), Color.Transparent))))
    Box(Modifier.size(460.dp).offset(120.dp, 330.dp).background(Brush.radialGradient(listOf(glow2.copy(alpha = 0.30f), Color.Transparent))))
    // Fine dot grid gives depth without distracting.
    Canvas(Modifier.fillMaxSize()) {
        val step = 22.dp.toPx()
        var y = step / 2
        while (y < size.height) {
            var x = step / 2
            while (x < size.width) { drawCircle(Color.White.copy(alpha = 0.06f), 1.2f, Offset(x, y)); x += step }
            y += step
        }
    }
}

@Composable
private fun Pill(text: String, modifier: Modifier = Modifier, bg: Color = Color.White.copy(alpha = 0.12f), fg: Color = Color.White) =
    Text(text, style = tx(15.sp, fg), modifier = modifier.background(bg, RoundedCornerShape(50)).border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(50))
        .padding(horizontal = 16.dp, vertical = 6.dp))

@Composable
private fun GlassCard(modifier: Modifier = Modifier, border: Color = Color.White.copy(alpha = 0.25f), content: @Composable () -> Unit) =
    Box(modifier.shadow(18.dp, RoundedCornerShape(22.dp)).background(Color(0xFF151A3D).copy(alpha = 0.92f), RoundedCornerShape(22.dp))
        .border(1.5.dp, border, RoundedCornerShape(22.dp)).padding(horizontal = 20.dp, vertical = 14.dp)) { content() }

private fun screen(name: String) = listOf("../marketing/ad-screens", "marketing/ad-screens").map { File(it, "$name.png") }.first { it.exists() }
    .readBytes().let { BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap() }

@Composable
private fun Phone(name: String, width: Dp, modifier: Modifier = Modifier) {
    Box(modifier.width(width).height(width * 2.222f + 14.dp).shadow(28.dp, RoundedCornerShape(width / 6.5f))
        .background(Color(0xFF0B0B0F), RoundedCornerShape(width / 6.5f)).border(2.dp, Color(0xFF3A3F55), RoundedCornerShape(width / 6.5f))
        .padding(7.dp)) {
        Image(screen(name), null, Modifier.fillMaxSize().clip(RoundedCornerShape(width / 7.8f)), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter)
        Box(Modifier.align(Alignment.TopCenter).padding(top = 6.dp).size(width / 4, 5.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(50)))
    }
}

@Composable
private fun FeatureCaption(kicker: String, title: String, sub: String) = Layer {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Pill(kicker, bg = BrandGold, fg = BrandNavy)
        Spacer(Modifier.height(8.dp))
        Text(title, style = tx(30.sp))
        Text(sub, style = tx(16.sp, Color.White.copy(alpha = 0.85f), FontWeight.SemiBold))
    }
}

// ---- Scene 1: Reading problem --------------------------------------------------------------

@Composable private fun S1Label() = Layer { Pill("📖  IELTS Reading", Modifier.align(Alignment.TopCenter).padding(top = 70.dp)) }

@Composable private fun S1Question() = Layer {
    GlassCard(Modifier.align(Alignment.TopCenter).padding(top = 130.dp).width(312.dp)) {
        Column {
            Text("প্রশ্নে লেখা", style = tx(15.sp, Color.White.copy(alpha = 0.7f), FontWeight.SemiBold, TextAlign.Start))
            Text("“reduce the harm”", style = tx(30.sp, Color.White, align = TextAlign.Start))
        }
    }
}

@Composable private fun S1Passage() = Layer {
    GlassCard(Modifier.align(Alignment.TopCenter).padding(top = 262.dp).width(312.dp), border = BrandGold) {
        Column {
            Text("প্যাসেজে লেখা", style = tx(15.sp, BrandGold.copy(alpha = 0.85f), FontWeight.SemiBold, TextAlign.Start))
            Text("“mitigate”", style = tx(30.sp, BrandGold, align = TextAlign.Start))
        }
    }
}

@Composable private fun S1Same() = Layer {
    Text("একই কথা, ভিন্ন শব্দ…", style = tx(24.sp), modifier = Modifier.align(Alignment.TopCenter).padding(top = 402.dp))
}

@Composable private fun S1Stamp() = Layer {
    Text("মিস! ✗", style = tx(44.sp, WrongRed), modifier = Modifier.align(Alignment.TopCenter).padding(top = 462.dp).rotate(-8f)
        .border(4.dp, WrongRed, RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(14.dp))
        .padding(horizontal = 22.dp, vertical = 4.dp))
}

// ---- Scene 2: Listening problem ------------------------------------------------------------

@Composable private fun S2Label() = Layer { Pill("🎧  IELTS Listening", Modifier.align(Alignment.TopCenter).padding(top = 70.dp)) }

@Composable private fun S2Heard() = Layer {
    GlassCard(Modifier.align(Alignment.TopCenter).padding(top = 140.dp).width(312.dp)) {
        Column {
            Text("শুনলেন", style = tx(15.sp, Color.White.copy(alpha = 0.7f), FontWeight.SemiBold, TextAlign.Start))
            Text("🔊  “accommodation”", style = tx(26.sp, Color.White, align = TextAlign.Start))
        }
    }
}

@Composable private fun S2Wrote() = Layer {
    GlassCard(Modifier.align(Alignment.TopCenter).padding(top = 262.dp).width(312.dp), border = WrongRed) {
        Column {
            Text("লিখলেন", style = tx(15.sp, Color.White.copy(alpha = 0.7f), FontWeight.SemiBold, TextAlign.Start))
            Text(buildAnnotatedString {
                append("a"); withStyle(SpanStyle(color = WrongRed, textDecoration = TextDecoration.Underline)) { append("c") }
                append("o"); withStyle(SpanStyle(color = WrongRed, textDecoration = TextDecoration.Underline)) { append("m") }
                append("odation")
            }, style = tx(32.sp, Color.White, align = TextAlign.Start))
        }
    }
}

@Composable private fun S2Rule() = Layer {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 410.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("একটি বানান ভুল", style = tx(26.sp))
        Text("= শূন্য নম্বর", style = tx(40.sp, WrongRed),
            modifier = Modifier.padding(top = 6.dp).background(Color.White, RoundedCornerShape(16.dp)).padding(horizontal = 20.dp, vertical = 2.dp))
    }
}

// ---- Scene 3: brand turn --------------------------------------------------------------------

@Composable private fun S3Line() = Layer {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 150.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("এবার প্রস্তুতি হোক", style = tx(32.sp))
        Text("বুঝে ✨", style = tx(46.sp, BrandGold))
    }
}

@Composable private fun S3Logo() = Layer {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 300.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(150.dp).shadow(30.dp, CircleShape).background(BrandNavy, CircleShape).border(3.dp, BrandGold, CircleShape),
            contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(150.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("Vocabsolution", style = tx(36.sp))
        Text("IELTS-এর শব্দভান্ডার, বাংলায় বুঝে", style = tx(17.sp, Color.White.copy(alpha = 0.85f), FontWeight.SemiBold))
    }
}

// ---- Scenes 4–6: features -------------------------------------------------------------------

@Composable private fun PhoneCenter(name: String) = Layer { Phone(name, 214.dp, Modifier.align(Alignment.TopCenter).padding(top = 156.dp)) }

@Composable private fun ParaChip() = Layer {
    Text("mitigate  ≈  reduce the harm  ✓", style = tx(19.sp, Color.White),
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 330.dp).shadow(16.dp, RoundedCornerShape(50))
            .background(CorrectGreen, RoundedCornerShape(50)).padding(horizontal = 18.dp, vertical = 10.dp))
}

@Composable private fun TwoPhones() = Layer {
    Row(Modifier.align(Alignment.TopCenter).padding(top = 170.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Phone("12_listen_and_spell", 158.dp, Modifier.offset(y = 10.dp))
        Phone("13_write_graph_quiz", 158.dp, Modifier.offset(y = 40.dp))
    }
}

// ---- Scene 7: the dream -------------------------------------------------------------------

/** Flight path from Dhaka; tools/marketing/build_concept3.py moves the plane along the same curve. */
val PathStart = Offset(48f, 560f)
val PathControl = Offset(70f, 220f)
val PathEnd = Offset(318f, 196f)

@Composable private fun S7Text() = Layer {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("স্বপ্নের পথে", style = tx(38.sp))
        Text("প্রস্তুতি হোক বুঝে", style = tx(28.sp, BrandGold))
    }
}

@Composable private fun S7Path() = Layer {
    Canvas(Modifier.fillMaxSize()) {
        val d = density
        val p = Path().apply {
            moveTo(PathStart.x * d, PathStart.y * d)
            quadraticTo(PathControl.x * d, PathControl.y * d, PathEnd.x * d, PathEnd.y * d)
        }
        drawPath(p, Color.White.copy(alpha = 0.7f), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 9.dp.toPx()))))
        drawCircle(BrandGold, 7.dp.toPx(), Offset(PathStart.x * d, PathStart.y * d))
        drawCircle(BrandGold, 7.dp.toPx(), Offset(PathEnd.x * d, PathEnd.y * d))
    }
    Pill("📍 ঢাকা", Modifier.offset(x = 20.dp, y = 576.dp), bg = Color.White.copy(alpha = 0.18f))
    Column(Modifier.offset(x = 128.dp, y = 148.dp), horizontalAlignment = Alignment.End) {
        Pill("🎓 UK · Canada · Australia", bg = Color.White.copy(alpha = 0.18f))
    }
}

/** The plane sits at the top-left corner; the build script offsets it along the curve. */
@Composable private fun Plane() = Layer { Text("✈️", style = tx(40.sp), modifier = Modifier.size(56.dp)) }

class Concept3LayersTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1920, xdpi = 440, ydpi = 440, density = Density.XXHIGH),
        theme = "android:Theme.Translucent.NoTitleBar",
        useDeviceResolution = true,
    )

    private fun shot(name: String, content: @Composable () -> Unit) = paparazzi.snapshot(name = name) { content() }

    @Test fun bgNight() = shot("bg_night") { Background(Night1, Night2, Glow, Color(0xFF9C4DFF)) }
    @Test fun bgBright() = shot("bg_bright") { Background(Color(0xFF0D2C54), Color(0xFF1F4A80), Color(0xFF42A5F5), BrandGold) }
    @Test fun bgDawn() = shot("bg_dawn") { Background(Color(0xFF2B1B54), Color(0xFFF08A4B), Color(0xFFFF7EB3), BrandGold) }

    @Test fun s1Label() = shot("s1_label") { S1Label() }
    @Test fun s1Question() = shot("s1_question") { S1Question() }
    @Test fun s1Passage() = shot("s1_passage") { S1Passage() }
    @Test fun s1Same() = shot("s1_same") { S1Same() }
    @Test fun s1Stamp() = shot("s1_stamp") { S1Stamp() }
    @Test fun s2Label() = shot("s2_label") { S2Label() }
    @Test fun s2Heard() = shot("s2_heard") { S2Heard() }
    @Test fun s2Wrote() = shot("s2_wrote") { S2Wrote() }
    @Test fun s2Rule() = shot("s2_rule") { S2Rule() }
    @Test fun s3Line() = shot("s3_line") { S3Line() }
    @Test fun s3Logo() = shot("s3_logo") { S3Logo() }
    @Test fun s4Caption() = shot("s4_caption") { FeatureCaption("Read Smart", "প্রসঙ্গের ভেতরে শব্দ", "১৫০টি প্যাসেজ · ১,৫০০টি একাডেমিক শব্দ") }
    @Test fun s4Phone() = shot("s4_phone") { PhoneCenter("10_read_smart_passage") }
    @Test fun s5Caption() = shot("s5_caption") { FeatureCaption("Paraphrase Bank", "একই কথা, ভিন্ন রূপ", "১,১৫২টি collocation ও paraphrase") }
    @Test fun s5Phone() = shot("s5_phone") { PhoneCenter("11_paraphrase_bank") }
    @Test fun s5Chip() = shot("s5_chip") { ParaChip() }
    @Test fun s6Caption() = shot("s6_caption") { FeatureCaption("Listening · Writing", "শুনে বানান, গ্রাফের ভাষা", "বানানের ফাঁদ · Task 1 ও Task 2") }
    @Test fun s6Phones() = shot("s6_phones") { TwoPhones() }
    @Test fun s7Text() = shot("s7_text") { S7Text() }
    @Test fun s7Path() = shot("s7_path") { S7Path() }
    @Test fun s7Plane() = shot("s7_plane") { Plane() }
}
