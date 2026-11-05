package top.stellortus.stellar_music

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.Density
import androidx.test.espresso.Espresso.pressBack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import top.stellortus.stellar_music.ui.BottomTab
import top.stellortus.stellar_music.ui.components.PlayerBarContent
import top.stellortus.stellar_music.ui.components.StellarNavigationBar
import top.stellortus.stellar_music.ui.player.PlaybackQueueHost
import top.stellortus.stellar_music.ui.player.PlaylistPlaybackController
import top.stellortus.stellar_music.ui.theme.StellarMusicTheme
import top.stellortus.stellar_music.ui.theme.StellarTheme
import top.stellortus.stellar_music.ui.widgets.StellarScaffold
import top.stellortus.stellar_music_common.dto.Track

class PlaybackQueueUiTest {
    @get:Rule val compose = createComposeRule()
    private val tracks = (1..3).map { Track(it, "歌曲$it", listOf("歌手$it"), "$it.mp3", "", "", "", 0L) }
    private var opens = 0
    private var toggles = 0
    private var nexts = 0
    private var previous = 0
    private var seekFinishes = 0
    private var seekValue = 0f
    private var panelBottomSpacingPx = 0

    private fun showPlayer(
        queue: List<Track> = tracks,
        initialIndex: Int = 0,
        initiallyOpen: Boolean = false,
        darkTheme: Boolean = false,
        fontScale: Float = 1f,
    ) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                StellarMusicTheme(darkTheme = darkTheme) {
                    val bottomSpacingPx = with(density) { StellarTheme.spacing.page.roundToPx() }
                    SideEffect { panelBottomSpacingPx = bottomSpacingPx }
                    var visible by remember { mutableStateOf(initiallyOpen) }
                    var selected by remember { mutableIntStateOf(initialIndex) }
                    var tab by remember { mutableStateOf(BottomTab.HomeTab) }
                    val scope = rememberCoroutineScope()
                    val controller = remember(queue) {
                        PlaylistPlaybackController(
                            scope, { emptyList() }, {}, { throw AssertionError(it) },
                            onQueueChanged = { _, index -> selected = index },
                        ).also { it.playPlaylist(queue, initialIndex) }
                    }
                    var playbackMode by remember { mutableStateOf(controller.mode) }
                    PlaybackQueueHost(visible, queue, selected, controller::playQueueIndex, { visible = false },
                        playbackMode = playbackMode,
                        onCyclePlaybackMode = {
                            controller.cycleMode()
                            playbackMode = controller.mode
                        },
                        modifier = Modifier.testTag("screen")) { anchor ->
                        StellarScaffold(
                            bottomBar = {
                                Column {
                                    Box(Modifier.testTag("playerLayout")) {
                                        PlayerBarContent(queue[selected], false, 1000f, 100000f,
                                            { toggles++ }, { seekValue = it }, { seekFinishes++ },
                                            onOpenQueue = { opens++; visible = true },
                                            onNextTrack = { nexts++; controller.next() },
                                            onPreviousTrack = { previous++; controller.previous() },
                                            cardAnchorModifier = anchor.testTag("playerAnchor"))
                                    }
                                    Box(Modifier.testTag("navigation")) {
                                        StellarNavigationBar(tab) { visible = false; tab = it }
                                    }
                                }
                            },
                        ) { padding ->
                            Box(Modifier.fillMaxSize().padding(padding).testTag("page"))
                        }
                    }
                }
            }
        }
    }

    private fun SemanticsNodeInteraction.dragUp(distanceDp: Float = 100f) = performTouchInput {
        swipe(center, center - Offset(0f, distanceDp * compose.density.density), 400)
    }

    private fun SemanticsNodeInteraction.dragHorizontally(distanceDp: Float) = performTouchInput {
        swipe(center, center + Offset(distanceDp * compose.density.density, 0f), 400)
    }

    private fun playerText() = compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions))

    private fun panel() = compose.onNodeWithContentDescription("当前播放列表面板")

    private fun assertOverlayStopsAbovePlayer() {
        val player = compose.onNodeWithTag("playerLayout").fetchSemanticsNode().boundsInRoot
        val anchor = compose.onNodeWithTag("playerAnchor").fetchSemanticsNode().boundsInRoot
        val scrim = compose.onNodeWithContentDescription("收起当前播放列表").fetchSemanticsNode().boundsInRoot
        assertEquals(player.top, anchor.top, 1f)
        assertEquals(player.top, scrim.bottom, 1f)
        assertTrue(panel().fetchSemanticsNode().boundsInRoot.bottom <= player.top + 1f)
    }

    private fun modeButton() = compose.onNodeWithContentDescription("切换播放模式")

    private fun assertMode(label: String) {
        modeButton().assertIsDisplayed().assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, label),
        )
    }

    @Test fun titleAndArtistHorizontalSwipesNavigateInBothDirectionsAndWrap() {
        showPlayer(initialIndex = 1)
        compose.onNodeWithText("歌曲2").dragHorizontally(-100f)
        compose.onNodeWithText("歌曲3").assertIsDisplayed()
        compose.onNodeWithText("歌手3").dragHorizontally(100f)
        compose.onNodeWithText("歌曲2").assertIsDisplayed()
        compose.onNodeWithText("歌曲2").dragHorizontally(100f)
        compose.onNodeWithText("歌曲1").assertIsDisplayed()
        compose.onNodeWithText("歌手1").dragHorizontally(100f)
        compose.onNodeWithText("歌曲3").assertIsDisplayed()
        compose.onNodeWithText("歌曲3").dragHorizontally(-100f)
        compose.onNodeWithText("歌曲1").assertIsDisplayed()
        panel().assertDoesNotExist()
        compose.runOnIdle {
            assertEquals(2, nexts); assertEquals(3, previous)
            assertEquals(0, opens); assertEquals(0, toggles); assertEquals(0, seekFinishes)
        }
    }

    @Test fun shortHorizontalSwipesAndDownwardSwipesDoNothing() {
        showPlayer(initialIndex = 1)
        compose.onNodeWithText("歌曲2").dragHorizontally(-20f)
        compose.onNodeWithText("歌手2").dragHorizontally(20f)
        compose.onNodeWithText("歌曲2").dragUp(-100f)
        compose.onNodeWithText("歌曲2").assertIsDisplayed()
        panel().assertDoesNotExist()
        compose.runOnIdle { assertEquals(0, nexts); assertEquals(0, previous); assertEquals(0, opens) }
    }

    @Test fun horizontalGestureKeepsItsAxisWhenItDriftsUpward() {
        showPlayer()
        playerText().performTouchInput {
            val scale = compose.density.density
            down(center)
            moveBy(Offset(-30f, -4f) * scale)
            moveBy(Offset(0f, -100f) * scale)
            moveBy(Offset(-80f, 0f) * scale)
            up()
        }
        compose.onNodeWithText("歌曲2").assertIsDisplayed()
        panel().assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, nexts); assertEquals(0, previous); assertEquals(0, opens) }
    }

    @Test fun verticalGestureKeepsItsAxisWhenItDriftsSideways() {
        showPlayer()
        playerText().performTouchInput {
            val scale = compose.density.density
            down(center)
            moveBy(Offset(0f, -30f) * scale)
            moveBy(Offset(-100f, 0f) * scale)
            moveBy(Offset(0f, -60f) * scale)
            up()
        }
        panel().assertIsDisplayed()
        compose.runOnIdle { assertEquals(0, nexts); assertEquals(0, previous); assertEquals(1, opens) }
    }

    @Test fun longGestureOnlyNavigatesOnceAcrossTrackRecompositionAndReversal() {
        showPlayer()
        playerText().performTouchInput {
            down(center)
            moveBy(Offset(-70f * compose.density.density, 0f))
        }
        compose.onNodeWithText("歌曲2").assertIsDisplayed()
        playerText().performTouchInput {
            val scale = compose.density.density
            moveBy(Offset(-80f * scale, 0f))
            moveBy(Offset(180f * scale, 0f))
            moveBy(Offset(-100f * scale, 0f))
            up()
        }
        compose.onNodeWithText("歌曲2").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, nexts); assertEquals(0, previous); assertEquals(0, opens) }
        playerText().dragHorizontally(100f)
        compose.onNodeWithText("歌曲1").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, nexts); assertEquals(1, previous) }
    }

    @Test fun manualSwipesNavigateAndReturnInEveryPlaybackMode() {
        showPlayer(initialIndex = 1)
        listOf("循环播放", "随机播放", "单曲循环").forEachIndexed { index, label ->
            playerText().dragUp()
            if (index > 0) modeButton().performClick()
            assertMode(label)
            panel().performSemanticsAction(SemanticsActions.Dismiss) { it() }
            playerText().dragHorizontally(-100f)
            compose.onNodeWithText("歌曲2").assertDoesNotExist()
            playerText().dragHorizontally(100f)
            compose.onNodeWithText("歌曲2").assertIsDisplayed()
        }
        compose.runOnIdle { assertEquals(3, nexts); assertEquals(3, previous) }
    }

    @Test fun accessibilityActionsNavigateInBothDirections() {
        showPlayer(initialIndex = 1)
        val next = playerText().fetchSemanticsNode().config[SemanticsActions.CustomActions]
            .first { it.label == "下一首" }
        compose.runOnIdle { assertTrue(next.action()) }
        compose.onNodeWithText("歌曲3").assertIsDisplayed()
        val back = playerText().fetchSemanticsNode().config[SemanticsActions.CustomActions]
            .first { it.label == "上一首" }
        compose.runOnIdle { assertTrue(back.action()) }
        compose.onNodeWithText("歌曲2").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, nexts); assertEquals(1, previous); assertEquals(0, opens) }
    }

    @Test fun modeButtonCyclesWithoutSelectingOrClosingAndSurvivesReopening() {
        showPlayer(initiallyOpen = true, initialIndex = 1)
        assertMode("循环播放")
        val titleBounds = compose.onNodeWithText("当前播放列表（3）").fetchSemanticsNode().boundsInRoot
        assertTrue(modeButton().fetchSemanticsNode().boundsInRoot.left >= titleBounds.right)
        repeat(2) {
            listOf("随机播放", "单曲循环", "循环播放").forEach { label ->
                modeButton().performClick()
                assertMode(label)
                panel().assertIsDisplayed()
                compose.onNode(isSelected() and hasAnyDescendant(hasText("歌曲2"))).assertExists()
            }
        }
        modeButton().performClick()
        assertMode("随机播放")
        panel().performSemanticsAction(SemanticsActions.Dismiss) { it() }
        panel().assertDoesNotExist()
        compose.onNodeWithText("歌曲2").dragUp()
        assertMode("随机播放")
        compose.runOnIdle { assertEquals(0, toggles); assertEquals(0, seekFinishes) }
    }

    @Test fun titleSwipeOpensQueueAndSelectionKeepsItOpen() {
        showPlayer()
        compose.onNodeWithText("歌曲1").dragUp()
        compose.onNodeWithText("当前播放列表（3）").assertIsDisplayed()
        compose.onNode(isSelected() and hasAnyDescendant(hasText("歌曲1"))).assertExists()
        compose.onNode(hasText("歌曲2") and hasClickAction()).performClick()
        compose.onNode(isSelected() and hasAnyDescendant(hasText("歌曲2"))).assertExists()
        compose.onNodeWithText("当前播放列表（3）").assertIsDisplayed()
        compose.runOnIdle { assertEquals(1, opens); assertEquals(0, seekFinishes) }
        panel().performSemanticsAction(SemanticsActions.Dismiss) { it() }
        panel().assertDoesNotExist()
    }

    @Test fun artistSwipeOpensButShortSwipeDoesNot() {
        showPlayer()
        compose.onNodeWithText("歌手1").dragUp(20f)
        compose.runOnIdle { assertEquals(0, opens) }
        compose.onNodeWithText("歌手1").dragUp()
        compose.onNodeWithText("当前播放列表（3）").assertIsDisplayed()
    }

    @Test fun coverAndButtonSwipesDoNotOpenQueueAndPlaybackStillToggles() {
        showPlayer()
        compose.onNodeWithContentDescription("专辑封面，播放").dragUp()
        compose.onNodeWithContentDescription("播放").dragUp()
        compose.onNodeWithContentDescription("专辑封面，播放").dragHorizontally(-100f)
        compose.onNodeWithContentDescription("专辑封面，播放").dragHorizontally(100f)
        compose.onNodeWithContentDescription("播放").dragHorizontally(-100f)
        compose.onNodeWithContentDescription("播放").dragHorizontally(100f)
        compose.runOnIdle { assertEquals(0, opens); assertEquals(0, nexts); assertEquals(0, previous) }
        compose.onNodeWithContentDescription("播放").performClick()
        compose.runOnIdle { assertEquals(1, toggles) }
    }

    @Test fun panelIsAnchoredWithoutMovingPlayerNavigationOrPage() {
        showPlayer()
        val playerBefore = compose.onNodeWithTag("playerLayout").fetchSemanticsNode().boundsInRoot
        val playButtonBefore = compose.onNodeWithContentDescription("播放").fetchSemanticsNode().boundsInRoot
        val navigationBefore = compose.onNodeWithTag("navigation").fetchSemanticsNode().boundsInRoot
        val pageBefore = compose.onNodeWithTag("page").fetchSemanticsNode().boundsInRoot
        compose.onNodeWithText("歌曲1").dragUp()
        val bounds = panel().fetchSemanticsNode().boundsInRoot
        assertOverlayStopsAbovePlayer()
        assertEquals(playerBefore.top - panelBottomSpacingPx, bounds.bottom, 1f)
        assertTrue(bounds.top < playerBefore.top)
        assertEquals(playerBefore, compose.onNodeWithTag("playerLayout").fetchSemanticsNode().boundsInRoot)
        assertEquals(playButtonBefore, compose.onNodeWithContentDescription("播放").fetchSemanticsNode().boundsInRoot)
        assertEquals(navigationBefore, compose.onNodeWithTag("navigation").fetchSemanticsNode().boundsInRoot)
        assertEquals(pageBefore, compose.onNodeWithTag("page").fetchSemanticsNode().boundsInRoot)
        compose.onNodeWithContentDescription("播放").performClick()
        compose.runOnIdle { assertEquals(1, toggles) }
        panel().assertIsDisplayed()
        compose.onNodeWithText("歌单").performClick()
        panel().assertDoesNotExist()
        assertEquals(playerBefore, compose.onNodeWithTag("playerLayout").fetchSemanticsNode().boundsInRoot)
        assertEquals(playButtonBefore, compose.onNodeWithContentDescription("播放").fetchSemanticsNode().boundsInRoot)
    }

    @Test fun scrimBackAndHeaderSwipeCloseThePanel() {
        showPlayer()
        compose.onNodeWithText("歌曲1").dragUp()
        compose.onNodeWithContentDescription("收起当前播放列表").performTouchInput { click(topCenter + Offset(0f, 5f)) }
        panel().assertDoesNotExist()
        compose.onNodeWithText("歌曲1").dragUp()
        pressBack()
        panel().assertDoesNotExist()
        compose.onNodeWithText("歌曲1").dragUp()
        compose.onNodeWithContentDescription("下滑收起播放列表").performTouchInput {
            swipe(center, center + Offset(0f, 100f * compose.density.density), 400)
        }
        panel().assertDoesNotExist()
    }

    @Test fun animationRevealsUpwardAndClipsAtThePlayerEdge() {
        showPlayer()
        val playerTop = compose.onNodeWithTag("playerLayout").fetchSemanticsNode().boundsInRoot.top
        val action = compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions))
            .fetchSemanticsNode().config[SemanticsActions.CustomActions].first()
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { action.action() }
        compose.mainClock.advanceTimeBy(80)
        val enteringTop = panel().fetchSemanticsNode().boundsInRoot.top
        assertOverlayStopsAbovePlayer()
        assertTrue(panel().fetchSemanticsNode().boundsInRoot.bottom <= playerTop + 1f)
        compose.mainClock.advanceTimeBy(300)
        assertOverlayStopsAbovePlayer()
        val expandedTop = panel().fetchSemanticsNode().boundsInRoot.top
        assertTrue(enteringTop > expandedTop)
        panel().performSemanticsAction(SemanticsActions.Dismiss) { it() }
        compose.mainClock.advanceTimeBy(80)
        assertTrue(panel().fetchSemanticsNode().boundsInRoot.top > expandedTop)
        assertOverlayStopsAbovePlayer()
        compose.mainClock.autoAdvance = true
        panel().assertDoesNotExist()
    }

    @Test fun longQueueLocatesCurrentSongWithLargeTextAndDarkTheme() {
        val longQueue = (1..50).map { Track(it, "长队列歌曲$it", listOf("歌手"), "$it.mp3", "", "", "", 0L) }
        showPlayer(longQueue, initialIndex = 35, initiallyOpen = true, darkTheme = true, fontScale = 1.5f)
        compose.onNodeWithText("当前播放列表（50）").assertIsDisplayed()
        assertOverlayStopsAbovePlayer()
        assertMode("循环播放")
        modeButton().performClick()
        assertMode("随机播放")
        compose.onNode(hasText("长队列歌曲36") and hasClickAction()).assertIsDisplayed()
        compose.onNode(isSelected() and hasAnyDescendant(hasText("长队列歌曲36"))).assertExists()
        val panelBounds = panel().fetchSemanticsNode().boundsInRoot
        val screenBounds = compose.onNodeWithTag("screen").fetchSemanticsNode().boundsInRoot
        assertTrue(panelBounds.height <= screenBounds.height * 0.65f + 1f)
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        compose.onNodeWithText("长队列歌曲1").assertIsDisplayed()
        panel().assertIsDisplayed()
    }

    @Test fun seekBarKeepsTapAndDragBehaviorWithoutOpeningQueue() {
        showPlayer()
        val seek = compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
        seek.performTouchInput { click(Offset(width * 0.6f, centerY)) }
        compose.runOnIdle { assertTrue(seekValue > 50000f); assertEquals(1, seekFinishes) }
        seek.performTouchInput { swipe(Offset(width * 0.2f, centerY), Offset(width * 0.8f, centerY), 400) }
        compose.runOnIdle { assertTrue(seekValue > 70000f); assertEquals(2, seekFinishes) }
        seek.dragUp()
        compose.runOnIdle { assertEquals(0, opens); assertEquals(0, nexts); assertEquals(0, previous) }
    }
}
