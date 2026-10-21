package top.stellortus.stellar_music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import top.stellortus.stellar_music.R
import top.stellortus.stellar_music.ui.theme.StellarTheme

enum class CoverPlaceholderSize { Small, Medium, Large }

@Composable
fun CoverPlaceholder(size: CoverPlaceholderSize, modifier: Modifier = Modifier) {
    val artworkSize = when (size) {
        CoverPlaceholderSize.Small -> StellarTheme.sizes.coverSmall
        CoverPlaceholderSize.Medium -> StellarTheme.sizes.coverMedium
        CoverPlaceholderSize.Large -> StellarTheme.sizes.coverLarge
    }
    val iconSize = when (size) {
        CoverPlaceholderSize.Small -> StellarTheme.sizes.coverIconSmall
        CoverPlaceholderSize.Medium -> StellarTheme.sizes.coverIconMedium
        CoverPlaceholderSize.Large -> StellarTheme.sizes.coverIconLarge
    }
    Box(modifier.size(artworkSize)
        .clip(StellarTheme.shapes.cover)
        .background(StellarTheme.colors.coverGradient),
        contentAlignment = Alignment.Center) {
        StellarIcon(
            painter = painterResource(R.drawable.ic_music_notes),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tone = StellarTone.Cover
        )
    }
}

@Composable
fun FormCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    StellarCard(modifier = modifier, kind = StellarSurfaceKind.Card) {
        Column(Modifier.fillMaxWidth().padding(StellarTheme.spacing.page),
            verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.section), content = content)
    }
}

@Composable
fun ProfileIdentity(username: String, userId: Int?) {
    FormCard {
        StellarCard( kind = StellarSurfaceKind.Avatar) {
            Box(Modifier.size(StellarTheme.sizes.avatarSize), contentAlignment = Alignment.Center) {
                StellarText(username.take(1).uppercase().ifBlank { "♪" }, role = StellarTextRole.Avatar)
            }
        }
        StellarText(username.ifBlank { "音乐爱好者" }, role = StellarTextRole.Title)
        userId?.let { StellarText("账号 ID：$it", role = StellarTextRole.BodySmall, tone = StellarTone.Secondary) }
    }
}
