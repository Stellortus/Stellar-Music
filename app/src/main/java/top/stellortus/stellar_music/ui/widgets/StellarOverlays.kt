package top.stellortus.stellar_music.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import top.stellortus.stellar_music.ui.theme.StellarTheme

enum class StellarMenuActionKind { Normal, Danger }
data class StellarMenuAction(
    val label: String,
    val iconRes: Int,
    val onClick: () -> Unit,
    val kind: StellarMenuActionKind = StellarMenuActionKind.Normal
)

@Composable
fun StellarActionMenu(expanded: Boolean, onDismiss: () -> Unit, actions: List<StellarMenuAction>) {
    DropdownMenu(
        expanded,
        onDismiss,
        modifier = Modifier.widthIn(min = StellarTheme.sizes.menuMinWidth),
        shape = StellarTheme.shapes.menu,
        containerColor = StellarTheme.colors.overlayBackground,
        tonalElevation = StellarTheme.sizes.cardElevation,
        shadowElevation = StellarTheme.sizes.menuElevation
    ) {
        StellarMenuContent(actions, onDismiss)
    }
}

@Composable
internal fun StellarMenuContent(actions: List<StellarMenuAction>, onDismiss: () -> Unit = {}) {
    Column(
        Modifier.padding(horizontal = StellarTheme.spacing.gap),
        verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.gap)
    ) {
        actions.forEach { action ->
            StellarButton(
                modifier = Modifier.fillMaxWidth(),
                kind = if (action.kind == StellarMenuActionKind.Danger) StellarButtonKind.Danger else StellarButtonKind.Secondary,
                onClick = { onDismiss(); action.onClick() }) {
                StellarIcon(
                    painterResource(action.iconRes),
                    null,
                    Modifier.size(StellarTheme.sizes.menuIcon)
                )
                StellarText(
                    action.label,
                    Modifier
                        .padding(start = StellarTheme.spacing.gap)
                        .weight(1f),
                    role = StellarTextRole.Action
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StellarBottomSheet(onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = StellarTheme.shapes.sheet,
        containerColor = StellarTheme.colors.overlayBackground,
        contentColor = StellarTheme.colors.textPrimary,
        scrimColor = StellarTheme.colors.overlayScrim,
        tonalElevation = StellarTheme.sizes.cardElevation,
        content = content
    )
}
