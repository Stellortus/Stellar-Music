package top.stellortus.stellar_music.ui.components

import androidx.compose.runtime.Composable
import top.stellortus.stellar_music.R

@Composable
fun PlaylistActionsMenu(
    expanded: Boolean, onDismiss: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit,
    isPublic: Boolean = false, onToggleVisibility: (() -> Unit)? = null
) {
    StellarActionMenu(expanded, onDismiss, buildList {
        if (onToggleVisibility != null) add(StellarMenuAction(
            if (isPublic) "设为私有" else "设为公开", R.drawable.ic_edit, onToggleVisibility))
        addAll(listOf(
        StellarMenuAction("重命名", R.drawable.ic_edit, onRename),
        StellarMenuAction("删除", R.drawable.ic_delete, onDelete, StellarMenuActionKind.Danger)
        ))
    })
}
