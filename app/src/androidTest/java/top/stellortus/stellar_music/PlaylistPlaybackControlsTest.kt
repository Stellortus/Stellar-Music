package top.stellortus.stellar_music

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import top.stellortus.stellar_music.ui.components.CoverPlaceholderSize
import top.stellortus.stellar_music.ui.components.PlaylistItem
import top.stellortus.stellar_music.ui.components.PlaylistPlayCover
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme
import top.stellortus.stellar_music_common.dto.Playlist

class PlaylistPlaybackControlsTest {
    @get:Rule val compose = createComposeRule()
    private var toggles = 0
    private var starts = 0
    private var opens = 0
    private lateinit var automaticPlayingChange: () -> Unit
    private lateinit var toggleDetailVisibility: () -> Unit

    private fun show(darkTheme: Boolean = false, fontScale: Float = 1f, initialSourceId: Int = 1) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                StellarMusicTheme(darkTheme = darkTheme) {
                    var sourceId by remember { mutableIntStateOf(initialSourceId) }
                    var playing by remember { mutableStateOf(true) }
                    var detailVisible by remember { mutableStateOf(true) }
                    var feedbackVersion by remember { mutableLongStateOf(0L) }
                    automaticPlayingChange = { playing = !playing }
                    toggleDetailVisibility = { detailVisible = !detailVisible }
                    val play: (Int) -> Unit = { id ->
                        if (sourceId == id) { toggles++; playing = !playing }
                        else { starts++; sourceId = id; playing = true }
                        feedbackVersion++
                    }
                    Column {
                        Box(Modifier.testTag("list")) {
                            Column {
                                (1..2).forEach { id ->
                                    PlaylistItem(Playlist(id, 1, "歌单$id", null, null, false, 0L, 0L, 2),
                                        onOpen = { opens++ }, onRename = {}, onDelete = {},
                                        onPlay = { play(id) }, isCurrent = sourceId == id, isPlaying = playing,
                                        controlFeedbackVersion = feedbackVersion)
                                }
                            }
                        }
                        if (detailVisible) Box(Modifier.testTag("detail")) {
                            PlaylistPlayCover("歌单1", { play(1) }, size = CoverPlaceholderSize.Large,
                                isCurrent = sourceId == 1, isPlaying = playing,
                                controlFeedbackVersion = feedbackVersion)
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
    }

    private fun button(label: String, tag: String = "list") = compose.onNode(
        hasContentDescription(label) and hasClickAction() and hasAnyAncestor(hasTestTag(tag)))

    private fun overlay(tag: String = "list") = compose.onNode(
        hasTestTag("playlistPlaybackOverlay") and hasAnyAncestor(hasTestTag(tag)), useUnmergedTree = true)

    private fun clickPlayback(label: String, tag: String = "list") {
        button(label, tag).performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
    }

    private fun freezeAndPause() {
        compose.mainClock.autoAdvance = false
        clickPlayback("暂停歌单：歌单1")
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        overlay().assertExists()
    }

    @Test fun coversInitiallyHaveNoPlaybackIconsButRemainClickable() {
        show(initialSourceId = 0)
        compose.onAllNodesWithTag("playlistPlaybackOverlay", useUnmergedTree = true).assertCountEquals(0)
        clickPlayback("循环播放歌单：歌单2")
        overlay().assertExists()
        button("暂停歌单：歌单2").assertIsDisplayed()
        overlay("detail").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, starts); assertEquals(0, opens) }
    }

    @Test fun listAndDetailStayInSyncWhenPausingAndResuming() {
        show()
        clickPlayback("暂停歌单：歌单1")
        button("继续播放歌单：歌单1").assertIsDisplayed()
        overlay().assertExists()
        overlay("detail").assertExists()
        clickPlayback("继续播放歌单：歌单1", "detail")
        button("暂停歌单：歌单1").assertIsDisplayed()
        compose.runOnIdle { assertEquals(2, toggles); assertEquals(0, starts); assertEquals(0, opens) }
    }

    @Test fun feedbackStaysForTwoSecondsThenFadesInsteadOfDisappearingImmediately() {
        show()
        freezeAndPause()
        compose.mainClock.advanceTimeBy(1900)
        overlay().assertExists()
        overlay("detail").assertExists()
        compose.mainClock.advanceTimeBy(200)
        overlay().assertExists()
        overlay("detail").assertExists()
        compose.mainClock.advanceTimeBy(400)
        overlay().assertDoesNotExist()
        overlay("detail").assertDoesNotExist()
        button("继续播放歌单：歌单1").assertIsDisplayed()
        compose.mainClock.autoAdvance = true
    }

    @Test fun resumingDuringFadeRestoresPersistentDisplayAndPausingRestartsTimer() {
        show()
        freezeAndPause()
        compose.mainClock.advanceTimeBy(2100)
        clickPlayback("继续播放歌单：歌单1")
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeBy(5000)
        overlay().assertExists()
        clickPlayback("暂停歌单：歌单1")
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        overlay().assertExists()
        compose.mainClock.advanceTimeBy(2500)
        overlay().assertDoesNotExist()
        compose.runOnIdle { assertEquals(3, toggles); assertEquals(0, starts) }
        compose.mainClock.autoAdvance = true
    }

    @Test fun playbackIntentChangesKeepPlayingVisibleAndTimeOutOnlyWhenPaused() {
        show()
        compose.mainClock.advanceTimeBy(5000)
        overlay().assertExists()
        compose.runOnIdle { automaticPlayingChange() }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeBy(1900)
        overlay().assertExists()
        compose.mainClock.advanceTimeBy(600)
        overlay().assertDoesNotExist()
        compose.runOnIdle { automaticPlayingChange() }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeBy(5000)
        overlay().assertExists()
        compose.mainClock.autoAdvance = true
    }

    @Test fun switchingPlaylistHidesOldFeedbackImmediatelyAndTitleStillOpens() {
        show()
        clickPlayback("暂停歌单：歌单1")
        overlay("detail").assertExists()
        clickPlayback("循环播放歌单：歌单2")
        overlay("detail").assertDoesNotExist()
        button("循环播放歌单：歌单1").assertIsDisplayed()
        overlay().assertExists()
        compose.onNodeWithText("歌单2").performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, opens); assertEquals(1, starts) }
    }

    @Test fun activeIconIsCenteredOnBothCoverSizesInDarkThemeWithLargeText() {
        show(darkTheme = true, fontScale = 1.5f)
        clickPlayback("暂停歌单：歌单1")
        listOf("list", "detail").forEach { tag ->
            val cover = button("继续播放歌单：歌单1", tag).fetchSemanticsNode().boundsInRoot
            assertEquals(cover, overlay(tag).fetchSemanticsNode().boundsInRoot)
            val icon = compose.onNode(hasTestTag("playlistPlaybackIcon") and
                hasAnyAncestor(hasTestTag(tag)), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            assertEquals(cover.center.x, icon.center.x, 1f)
            assertEquals(cover.center.y, icon.center.y, 1f)
        }
    }

    @Test fun activeCoverLongPressOpensMenuWithoutTogglingOrHidingPlayback() {
        show()
        button("暂停歌单：歌单1").performTouchInput { longClick() }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("重命名").assertIsDisplayed()
        overlay().assertExists()
        compose.runOnIdle { assertEquals(0, toggles); assertEquals(0, starts); assertEquals(0, opens) }
    }

    @Test fun enteringDetailsShowsPlayingOverlayButDoesNotReplayExpiredPauseFeedback() {
        show()
        compose.runOnIdle { toggleDetailVisibility() }
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle { toggleDetailVisibility() }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        overlay("detail").assertExists()
        clickPlayback("暂停歌单：歌单1")
        compose.mainClock.advanceTimeBy(2500)
        overlay("detail").assertDoesNotExist()
        compose.runOnIdle { toggleDetailVisibility() }
        compose.mainClock.advanceTimeByFrame()
        compose.runOnIdle { toggleDetailVisibility() }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
        overlay("detail").assertDoesNotExist()
        clickPlayback("继续播放歌单：歌单1", "detail")
        compose.mainClock.advanceTimeBy(5000)
        overlay().assertExists()
        overlay("detail").assertExists()
    }
}
