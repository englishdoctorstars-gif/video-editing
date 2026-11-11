package com.englishdoctor.vocabsolution.store

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import com.englishdoctor.vocabsolution.R
import com.englishdoctor.vocabsolution.ui.theme.BrandGold
import com.englishdoctor.vocabsolution.ui.theme.BrandNavy
import com.englishdoctor.vocabsolution.ui.theme.BrandNavyLight
import com.englishdoctor.vocabsolution.ui.theme.HindSiliguri
import com.englishdoctor.vocabsolution.ui.theme.VocabTheme
import org.junit.Rule
import org.junit.Test

/** 512×512 hi-res icon (Play applies its own mask, so it is full-bleed). */
class StoreIconTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 512, screenHeight = 512, xdpi = 160, ydpi = 160, density = Density.MEDIUM),
        useDeviceResolution = true,
    )

    @Test
    fun icon() = paparazzi.snapshot(name = "icon") {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(BrandNavy, BrandNavyLight))), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(512.dp))
        }
    }
}

/** 1024×500 feature graphic. */
class StoreFeatureGraphicTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1024, screenHeight = 500, xdpi = 320, ydpi = 320, density = Density.XHIGH,
            orientation = ScreenOrientation.LANDSCAPE),
        useDeviceResolution = true,
    )

    @Test
    fun featureGraphic() = paparazzi.snapshot(name = "featureGraphic") {
        VocabTheme(dark = false) {
            val white = TextStyle(fontFamily = HindSiliguri, color = Color.White)
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(BrandNavy, BrandNavyLight))).padding(28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(150.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Vocabsolution", style = white.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold))
                        Text("by English Doctor", style = white.copy(color = BrandGold, fontSize = 18.sp, fontWeight = FontWeight.Bold))
                        Spacer(Modifier.size(6.dp))
                        Text("সবার জন্য একটি পূর্ণাঙ্গ শব্দভাণ্ডার সমাধান", style = white.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold))
                        Text("মুখস্থ নয়, বুঝে শেখা · BCS · ব্যাংক · IELTS", style = white.copy(fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f)))
                        Spacer(Modifier.size(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("১১টি অধ্যায়", "২৫০ MCQ", "ইন্টারনেট ছাড়াই").forEach {
                                Text(it, style = white.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy),
                                    modifier = Modifier.background(BrandGold, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
