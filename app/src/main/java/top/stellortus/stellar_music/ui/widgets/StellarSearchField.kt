package top.stellortus.stellar_music.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import top.stellortus.stellar_music.R
import top.stellortus.stellar_music.ui.theme.StellarTheme

/** The caller owns the value and handles optional search submissions. */
@Composable
fun StellarSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
    enabled: Boolean = true,
    onSearch: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val colors = StellarTheme.colors
    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
                .focusProperties { canFocus = onClick == null }
                .then(if (onClick != null) Modifier.clearAndSetSemantics {} else Modifier),
            enabled = enabled,
            readOnly = onClick != null,
            singleLine = true,
            placeholder = { StellarText(placeholder, tone = StellarTone.Secondary) },
            leadingIcon = { StellarIcon(painterResource(R.drawable.ic_search), contentDescription = null) },
            trailingIcon = if (onClick == null && value.isNotEmpty()) {
                {
                    StellarIconButton(onClick = { onValueChange("") }, enabled = enabled) {
                        StellarIcon(painterResource(R.drawable.ic_close), contentDescription = "清空搜索内容")
                    }
                }
            } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                keyboard?.hide()
                onSearch?.invoke(value)
            }),
            shape = StellarTheme.shapes.textField,
            textStyle = StellarTheme.typography.body,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.insetBackground,
                unfocusedContainerColor = colors.insetBackground,
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.outline,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedLeadingIconColor = colors.accent,
                unfocusedLeadingIconColor = colors.textSecondary
            )
        )
        if (onClick != null) {
            // Intercept the entire field, including text, so this mode never opens an editor.
            Box(Modifier.matchParentSize()
                .semantics { this.contentDescription = contentDescription ?: value.ifEmpty { placeholder } }
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick))
        }
    }
}
