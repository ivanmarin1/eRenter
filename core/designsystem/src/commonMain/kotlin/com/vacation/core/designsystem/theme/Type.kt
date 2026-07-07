package com.vacation.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vacation.core.designsystem.generated.resources.Res
import com.vacation.core.designsystem.generated.resources.nunito_black
import com.vacation.core.designsystem.generated.resources.nunito_bold
import com.vacation.core.designsystem.generated.resources.nunito_extrabold
import com.vacation.core.designsystem.generated.resources.nunito_regular
import com.vacation.core.designsystem.generated.resources.nunito_semibold
import org.jetbrains.compose.resources.Font

/**
 * The "eRenter" design is set in **Nunito** at heavy weights (400–900). The font is bundled as a
 * Compose resource (see composeResources/font) so it renders identically on Android, iOS and Desktop.
 * [Font] is a @Composable resource loader, so the family — and therefore [appTypography] — is built
 * inside composition.
 */
@Composable
fun nunitoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.nunito_regular, FontWeight.Normal),
    Font(Res.font.nunito_semibold, FontWeight.SemiBold),
    Font(Res.font.nunito_bold, FontWeight.Bold),
    Font(Res.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(Res.font.nunito_black, FontWeight.Black),
)

/**
 * Type scale lifted from the design. Titles are black (900), the "eyebrow" labels above them are
 * bold small-caps, body copy is bold. Material slots are reused so the screens pick up the scale
 * without touching every call site.
 */
@Composable
fun appTypography(): Typography {
    val family = nunitoFontFamily()
    return Typography(
        // Big tab titles ("July 2026", "Paste bookings").
        headlineMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Black, fontSize = 23.sp, lineHeight = 26.sp),
        headlineSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Black, fontSize = 19.sp, lineHeight = 24.sp),
        // Section / card headings.
        titleLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Black, fontSize = 20.sp, lineHeight = 24.sp),
        titleMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, lineHeight = 20.sp),
        titleSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, lineHeight = 18.sp),
        // Body copy.
        bodyLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
        bodyMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp),
        bodySmall = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
        // Chips, eyebrow labels, weekday headers, nav labels.
        labelLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, lineHeight = 16.sp),
        labelMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, lineHeight = 14.sp),
        labelSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, lineHeight = 13.sp),
    )
}
