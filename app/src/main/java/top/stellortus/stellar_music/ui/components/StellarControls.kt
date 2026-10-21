package top.stellortus.stellar_music.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import top.stellortus.stellar_music.ui.theme.StellarTheme

enum class StellarTextRole { Inherit, Title, ItemTitle, Body, BodySmall, Action, Caption, Micro, Avatar }
enum class StellarTone { Inherit, Primary, Secondary, Accent, Danger, OnPrimaryAction, OnSecondaryAction, OnSelected, OnDanger, Cover }
enum class StellarButtonKind { Primary, Secondary, Outlined, Text, Danger }
enum class StellarIndicatorSize { Regular, Small, Button }
enum class StellarSurfaceKind { Card, Inset, Selected, Item, Player, Avatar }

@Composable
private fun StellarTone.color() = when (this) {
    StellarTone.Inherit -> LocalContentColor.current
    StellarTone.Primary -> StellarTheme.colors.textPrimary
    StellarTone.Secondary -> StellarTheme.colors.textSecondary
    StellarTone.Accent -> StellarTheme.colors.accent
    StellarTone.Danger -> StellarTheme.colors.dangerForeground
    StellarTone.OnPrimaryAction -> StellarTheme.colors.onPrimaryAction
    StellarTone.OnSecondaryAction -> StellarTheme.colors.onSecondaryAction
    StellarTone.OnSelected -> StellarTheme.colors.onSelectedBackground
    StellarTone.OnDanger -> StellarTheme.colors.onDangerBackground
    StellarTone.Cover -> StellarTheme.colors.coverForeground
}

@Composable
fun StellarText(
    text: String, modifier: Modifier = Modifier, role: StellarTextRole = StellarTextRole.Inherit,
    tone: StellarTone = StellarTone.Inherit, maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip, textAlign: TextAlign? = null, softWrap: Boolean = true
) {
    val type = StellarTheme.typography
    val style = when (role) {
        StellarTextRole.Inherit -> LocalTextStyle.current
        StellarTextRole.Title -> type.title
        StellarTextRole.ItemTitle -> type.itemTitle
        StellarTextRole.Body -> type.body
        StellarTextRole.BodySmall -> type.bodySmall
        StellarTextRole.Action -> type.action
        StellarTextRole.Caption -> type.caption
        StellarTextRole.Micro -> type.micro
        StellarTextRole.Avatar -> type.avatar
    }
    Text(text, modifier, color = tone.color(), style = style, maxLines = maxLines,
        overflow = overflow, textAlign = textAlign, softWrap = softWrap)
}

@Composable
fun StellarButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, kind: StellarButtonKind = StellarButtonKind.Primary,
    enabled: Boolean = true, loading: Boolean = false, content: @Composable RowScope.() -> Unit
) {
    val colors = StellarTheme.colors
    val body: @Composable RowScope.() -> Unit = {
        if (loading) {
            StellarLoadingIndicator(Modifier.padding(end = StellarTheme.spacing.gap),
                size = StellarIndicatorSize.Button, tone = StellarTone.Inherit)
        }
        content()
    }
    when (kind) {
        StellarButtonKind.Primary -> Button(onClick, modifier, enabled = enabled && !loading,
            shape = StellarTheme.shapes.button,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAction, contentColor = colors.onPrimaryAction,
                disabledContainerColor = colors.disabledBackground, disabledContentColor = colors.disabledContent), content = body)
        StellarButtonKind.Secondary, StellarButtonKind.Danger -> FilledTonalButton(onClick, modifier, enabled = enabled && !loading,
            shape = StellarTheme.shapes.button,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (kind == StellarButtonKind.Danger) colors.dangerBackground else colors.secondaryAction,
                contentColor = if (kind == StellarButtonKind.Danger) colors.onDangerBackground else colors.onSecondaryAction,
                disabledContainerColor = colors.disabledBackground, disabledContentColor = colors.disabledContent), content = body)
        StellarButtonKind.Outlined -> OutlinedButton(onClick, modifier, enabled = enabled && !loading,
            shape = StellarTheme.shapes.button,
            border = BorderStroke(StellarTheme.sizes.controlBorder, if (enabled && !loading) colors.outline else colors.disabledContent),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accent, disabledContentColor = colors.disabledContent), content = body)
        StellarButtonKind.Text -> TextButton(onClick, modifier, enabled = enabled && !loading,
            shape = StellarTheme.shapes.button,
            colors = ButtonDefaults.textButtonColors(contentColor = colors.accent, disabledContentColor = colors.disabledContent), content = body)
    }
}

@Composable
fun StellarIcon(painter: Painter, contentDescription: String?, modifier: Modifier = Modifier, tone: StellarTone = StellarTone.Inherit) {
    Icon(painter, contentDescription, modifier, tint = tone.color())
}

@Composable
fun StellarIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable () -> Unit) {
    IconButton(onClick, modifier.sizeIn(minWidth = StellarTheme.sizes.touch, minHeight = StellarTheme.sizes.touch), enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(contentColor = StellarTheme.colors.textPrimary, disabledContentColor = StellarTheme.colors.disabledContent),
        content = content)
}

@Composable
fun StellarTextField(
    value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier,
    enabled: Boolean = true, singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(value, onValueChange, modifier, enabled = enabled, singleLine = singleLine,
        label = { StellarText(label) }, visualTransformation = visualTransformation, keyboardOptions = keyboardOptions,
        shape = StellarTheme.shapes.textField, textStyle = StellarTheme.typography.body,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = StellarTheme.colors.accent, unfocusedBorderColor = StellarTheme.colors.outline,
            focusedTextColor = StellarTheme.colors.textPrimary, unfocusedTextColor = StellarTheme.colors.textPrimary))
}

@Composable
fun StellarCheckbox(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Checkbox(checked, onCheckedChange, modifier, enabled = enabled,
        colors = CheckboxDefaults.colors(checkedColor = StellarTheme.colors.primaryAction, checkmarkColor = StellarTheme.colors.onPrimaryAction,
            uncheckedColor = StellarTheme.colors.outline, disabledCheckedColor = StellarTheme.colors.disabledContent,
            disabledUncheckedColor = StellarTheme.colors.disabledContent))
}

@Composable
fun StellarDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier, thickness = StellarTheme.sizes.hairline, color = StellarTheme.colors.divider)
}

@Composable
fun StellarLoadingIndicator(modifier: Modifier = Modifier, size: StellarIndicatorSize = StellarIndicatorSize.Regular, tone: StellarTone = StellarTone.Accent) {
    val dimension = if (size == StellarIndicatorSize.Regular) StellarTheme.sizes.loadingIndicator else StellarTheme.sizes.indicator
    CircularProgressIndicator(modifier.size(dimension), color = tone.color(),
        strokeWidth = if (size == StellarIndicatorSize.Regular) StellarTheme.sizes.progress else StellarTheme.sizes.indicatorStroke)
}

@Composable
fun StellarCard(modifier: Modifier = Modifier, kind: StellarSurfaceKind = StellarSurfaceKind.Card, content: @Composable () -> Unit) {
    val colors = StellarTheme.colors
    val background = when (kind) {
        StellarSurfaceKind.Card -> colors.cardBackground
        StellarSurfaceKind.Inset -> colors.insetBackground
        StellarSurfaceKind.Selected, StellarSurfaceKind.Avatar -> colors.selectedBackground
        StellarSurfaceKind.Item -> colors.itemBackground
        StellarSurfaceKind.Player -> colors.playerBackground
    }
    val foreground = when (kind) {
        StellarSurfaceKind.Selected, StellarSurfaceKind.Avatar -> colors.onSelectedBackground
        else -> colors.textPrimary
    }
    val shape = when (kind) {
        StellarSurfaceKind.Avatar -> StellarTheme.shapes.avatar
        StellarSurfaceKind.Selected, StellarSurfaceKind.Item, StellarSurfaceKind.Inset -> StellarTheme.shapes.item
        else -> StellarTheme.shapes.card
    }
    Surface(modifier, shape = shape, color = background, contentColor = foreground,
        tonalElevation = if (kind == StellarSurfaceKind.Player) StellarTheme.sizes.cardElevation else StellarTheme.sizes.flatElevation,
        content = content)
}
