package com.englishdoctor.vocabsolution.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.englishdoctor.vocabsolution.R

/** Hind Siliguri renders Bengali conjuncts and vowel signs correctly and covers Latin too. */
val HindSiliguri = FontFamily(
    Font(R.font.hind_siliguri_regular, FontWeight.Normal),
    Font(R.font.hind_siliguri_semibold, FontWeight.SemiBold),
    Font(R.font.hind_siliguri_bold, FontWeight.Bold),
)

/** IPA symbols are not in Hind Siliguri; the system sans font covers them. */
val IpaFamily = FontFamily.SansSerif

private fun style(size: Int, weight: FontWeight = FontWeight.Normal, line: Int = (size * 1.45).toInt()) =
    TextStyle(fontFamily = HindSiliguri, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp)

val VocabTypography = Typography(
    displaySmall = style(34, FontWeight.Bold),
    headlineLarge = style(30, FontWeight.Bold),
    headlineMedium = style(26, FontWeight.Bold),
    headlineSmall = style(22, FontWeight.SemiBold),
    titleLarge = style(20, FontWeight.SemiBold),
    titleMedium = style(17, FontWeight.SemiBold),
    titleSmall = style(15, FontWeight.SemiBold),
    bodyLarge = style(17),
    bodyMedium = style(16),
    bodySmall = style(14),
    labelLarge = style(15, FontWeight.SemiBold),
    labelMedium = style(13, FontWeight.SemiBold),
    labelSmall = style(12, FontWeight.SemiBold),
)
