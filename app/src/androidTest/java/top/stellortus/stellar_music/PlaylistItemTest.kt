package top.stellortus.stellar_music

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import top.stellortus.stellar_music.ui.components.PlaylistItem
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme
import top.stellortus.stellar_music_common.dto.Playlist

class PlaylistItemTest {
    @get:Rule val compose = createComposeRule()
    private var opens = 0
    private var plays = 0
    private var renames = 0

    private fun show(actionsEnabled: Boolean = true, showActions: Boolean = true) {
        compose.setContent {
            StellarMusicTheme {
                PlaylistItem(
                    Playlist(1, 1, "测试歌单", null, null, false, 0L, 0L, 2),
                    onOpen = { opens++ }, onPlay = { plays++ },
                    onRename = { renames++ }, onDelete = {}, onToggleVisibility = {},
                    actionsEnabled = actionsEnabled, showActions = showActions,
                )
            }
        }
    }

    @Test fun coverPlaysAndTitleOpensWithoutMoreButton() {
        show()
        compose.onNodeWithContentDescription("循环播放歌单：测试歌单").performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, plays); assertEquals(0, opens) }
        compose.onNodeWithText("测试歌单").performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, opens); assertEquals(1, plays) }
        compose.onNodeWithText("更多").assertDoesNotExist()
    }

    @Test fun longPressTitleOpensExistingActionsWithoutNavigating() {
        show()
        compose.onNodeWithText("测试歌单").performTouchInput { longClick() }
        compose.onNodeWithText("设为公开").assertIsDisplayed()
        compose.onNodeWithText("删除").assertIsDisplayed()
        compose.onNodeWithText("重命名").performClick()
        compose.runOnIdle { assertEquals(1, renames); assertEquals(0, plays); assertEquals(0, opens) }
    }

    @Test fun longPressCoverOpensMenuWithoutPlaying() {
        show()
        compose.onNodeWithContentDescription("循环播放歌单：测试歌单").performTouchInput { longClick() }
        compose.onNodeWithText("重命名").assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, plays); assertEquals(0, opens) }
    }

    @Test fun disabledActionsCannotBeOpened() {
        show(actionsEnabled = false)
        compose.onNodeWithText("测试歌单").performTouchInput { longClick() }
        compose.onNodeWithText("重命名").assertDoesNotExist()
    }

    @Test fun searchResultsHaveNoManagementMenu() {
        show(showActions = false)
        compose.onNodeWithText("测试歌单").performTouchInput { longClick() }
        compose.onNodeWithText("删除").assertDoesNotExist()
    }
}
