package top.stellortus.stellar_music.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import top.stellortus.stellar_music.R
import top.stellortus.stellar_music.ui.theme.StellarTheme

@Composable
fun PlaylistToolbar(onCreate: () -> Unit, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth().padding(horizontal = StellarTheme.spacing.page), horizontalArrangement = Arrangement.End) {
        StellarIconButton(onClick = onCreate, enabled = enabled) {
            StellarIcon(painterResource(R.drawable.ic_add), "添加歌单")
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    name: String, onNameChange: (String) -> Unit,
    isPublic: Boolean, onPublicChange: (Boolean) -> Unit,
    submitting: Boolean, onCreate: () -> Unit, onDismiss: () -> Unit
) {
    StellarAlertDialog(title = "新建歌单", onDismissRequest = { if (!submitting) onDismiss() },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(StellarTheme.spacing.itemGap)) {
                StellarTextField(name, onNameChange, "歌单名称", Modifier.fillMaxWidth(), enabled = !submitting)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    StellarCheckbox(isPublic, onPublicChange, enabled = !submitting)
                    StellarText("公开歌单")
                }
                StellarText(if (isPublic) "其他用户可以查看此歌单" else "仅自己可见", tone = StellarTone.Secondary)
            }
        },
        confirmButton = {
            StellarButton(onCreate, enabled = name.isNotBlank() && !submitting, loading = submitting) { StellarText("创建") }
        },
        dismissButton = {
            StellarButton(onDismiss, kind = StellarButtonKind.Text, enabled = !submitting) { StellarText("取消") }
        })
}
