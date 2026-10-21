package top.stellortus.stellar_music.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** Shared layout tokens; feature screens must read these through the theme. */
@Immutable
class StellarSpacing {
    val tiny = 2.dp
    val small = 4.dp
    val gap = 8.dp
    val itemGap = 12.dp
    val trackVerticalPadding = 8.dp
    val homeTrackMargin = 8.dp
    val section = 16.dp
    val page = 20.dp
    val spacious = 24.dp
    val large = 32.dp
}

@Immutable
class StellarSizes {
    val flatElevation = 0.dp
    val loadingIndicator = 40.dp
    val hairline = 0.5.dp
    val controlBorder = 1.dp
    val touch = 48.dp
    val coverSmall = 40.dp
    val coverMedium = 52.dp
    val coverLarge = 96.dp
    val coverIconSmall = 22.dp
    val coverIconMedium = 28.dp
    val coverIconLarge = 52.dp
    val avatarSize = 96.dp
    val loadingArea = 120.dp
    val dialogHeight = 360.dp
    val progress = 4.dp
    val thumb = 6.dp
    val seekHeight = 24.dp
    val indicator = 18.dp
    val indicatorStroke = 2.dp
    val cardElevation = 2.dp
    val menuMinWidth = 176.dp
    val menuElevation = 0.dp
    val menuIcon = 20.dp
    val contentWidth = 560.dp
}
val LocalStellarSpacing = staticCompositionLocalOf { StellarSpacing() }
val LocalStellarSizes = staticCompositionLocalOf { StellarSizes() }
