package top.stellortus.stellarmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import top.stellortus.stellarmusic.lang.Language
import top.stellortus.stellarmusic.lang.localNameOf
import top.stellortus.stellarmusic.ui.BottomTab
import top.stellortus.stellarmusic.ui.BottomTab.HomeTab
import top.stellortus.stellarmusic.ui.theme.StellarMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StellarMusicTheme {
                MusicApp()
            }
        }
    }
}

@Composable
private fun MusicApp() {
    var selectedItem by remember { mutableStateOf(HomeTab) }
    val language = Language.zh_CN

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = selectedItem == item,
                        onClick = { selectedItem = item },
                        icon = {
                            Icon(
                                painter = painterResource(item.iconRes),
                                contentDescription = "${item.name} icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = {
                            Text(
                                text = item.displayName.localNameOf(language),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            selectedItem.ui.invoke()
        }
    }
}
