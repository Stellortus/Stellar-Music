package top.stellortus.stellar_music.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.widgets.FormCard
import top.stellortus.stellar_music.ui.widgets.StellarCard
import top.stellortus.stellar_music.ui.widgets.StellarSurfaceKind
import top.stellortus.stellar_music.ui.widgets.StellarText
import top.stellortus.stellar_music.ui.widgets.StellarTextRole
import top.stellortus.stellar_music.ui.widgets.StellarTone

@Composable
fun ProfileIdentity(username: String, userId: Int?) {
    FormCard {
        StellarCard(kind = StellarSurfaceKind.Avatar) {
            Box(Modifier.size(StellarTheme.sizes.avatarSize), contentAlignment = Alignment.Center) {
                StellarText(
                    username.take(1).uppercase().ifBlank { "♪" },
                    role = StellarTextRole.Avatar
                )
            }
        }
        StellarText(username.ifBlank { "音乐爱好者" }, role = StellarTextRole.Title)
        userId?.let {
            StellarText(
                "账号 ID：$it",
                role = StellarTextRole.BodySmall,
                tone = StellarTone.Secondary
            )
        }
    }
}
