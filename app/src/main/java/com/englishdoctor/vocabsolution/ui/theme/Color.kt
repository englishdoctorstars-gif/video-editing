package com.englishdoctor.vocabsolution.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Brand
val BrandNavy = Color(0xFF0D2C54)
val BrandNavyLight = Color(0xFF1F4A80)
val BrandGold = Color(0xFFF2A900)

/** Parts of speech, as marked in the book: ■ noun ▲ verb ● adjective ◆ adverb ○ other. */
enum class Pos(val symbol: String, val abbr: String, val bn: String, val light: Color, val dark: Color) {
    NOUN("■", "n", "বিশেষ্য", Color(0xFF1565C0), Color(0xFF64B5F6)),
    VERB("▲", "v", "ক্রিয়া", Color(0xFFC62828), Color(0xFFEF9A9A)),
    ADJECTIVE("●", "adj", "বিশেষণ", Color(0xFF2E7D32), Color(0xFF81C784)),
    ADVERB("◆", "adv", "ক্রিয়া-বিশেষণ", Color(0xFF6A1B9A), Color(0xFFCE93D8)),
    OTHER("○", "", "অন্যান্য পদ", Color(0xFF546E7A), Color(0xFFB0BEC5));

    companion object {
        fun of(name: String?): Pos = when (name) {
            "noun" -> NOUN
            "verb" -> VERB
            "adjective" -> ADJECTIVE
            "adverb" -> ADVERB
            else -> OTHER
        }
    }
}

/** The four parts (পর্ব) of the book each have a theme colour. */
enum class BookPart(val color: Color, val darkColor: Color) {
    FOUNDATION(Color(0xFF00897B), Color(0xFF4DB6AC)),
    USAGE(Color(0xFF3949AB), Color(0xFF7986CB)),
    IELTS(Color(0xFFF4511E), Color(0xFFFF8A65)),
    EXAM(Color(0xFFC2185B), Color(0xFFF06292)),
}

val CorrectGreen = Color(0xFF2E7D32)
val WrongRed = Color(0xFFC62828)
val HomophoneAmber = Color(0xFFFF8F00)

/** SRS stages of the 1-3-7-30 method. */
val StageColors = listOf(
    Color(0xFF90A4AE), // new
    Color(0xFF42A5F5), // day 1
    Color(0xFF26A69A), // day 3
    Color(0xFFFFA726), // day 7
    Color(0xFF7E57C2), // day 30
    Color(0xFF2E7D32), // mastered
)


@Composable
@ReadOnlyComposable
fun Pos.color(): Color = if (LocalDarkTheme.current) dark else light

@Composable
@ReadOnlyComposable
fun BookPart.tint(): Color = if (LocalDarkTheme.current) darkColor else color
