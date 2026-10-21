package top.stellortus.stellar_music.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import top.stellortus.stellar_music.ui.theme.dialogTitleStyle
import top.stellortus.stellar_music.ui.theme.StellarTheme

/** Shared dialog appearance; actions and loading state remain owned by the caller. */
@Composable
fun StellarAlertDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    text: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, style = StellarTheme.dialogTitleStyle) },
        text = text,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        shape = StellarTheme.shapes.dialog,
        containerColor = StellarTheme.colors.overlayBackground,
        titleContentColor = StellarTheme.colors.textPrimary,
        textContentColor = StellarTheme.colors.textSecondary,
        tonalElevation = StellarTheme.sizes.cardElevation
    )
}
