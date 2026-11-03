package top.stellortus.stellar_music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import top.stellortus.stellar_music.ui.player.PlaybackMode
import top.stellortus.stellar_music.ui.player.PlaybackOrder

class PlaybackOrderTest {
    @Suppress("SameParameterValue")
    private fun round(order: PlaybackOrder, size: Int) =
        List(size) { order.index.also { order.next() } }

    @Test fun cyclicOrderMovesImmediatelyAndWrapsBothWays() {
        val order = PlaybackOrder(5, 0)
        assertEquals(PlaybackMode.Cyclic, order.mode)
        assertEquals(1, order.next())
        assertEquals(1, order.index)
        assertEquals(2, order.step())
        assertEquals(1, order.previous())
        order.select(0)
        assertEquals(4, order.previous())
        assertEquals(0, order.next())
    }

    @Test fun shuffleVisitsEveryQueueIndexAndRepeatsTheSameOrderInBothDirections() {
        val order = PlaybackOrder(5, 2, PlaybackMode.Shuffle)
        val firstRound = round(order, 5)
        assertEquals(2, firstRound.first())
        assertEquals((0 until 5).toSet(), firstRound.toSet())
        repeat(3) { assertEquals(firstRound, round(order, 5)) }
        val backwardRound = List(5) { order.index.also { order.previous() } }
        assertEquals(listOf(firstRound.first()) + firstRound.drop(1).reversed(), backwardRound)
        assertEquals(2, order.index)
    }

    @Test fun shuffleCanNavigateFromAnyDirectSelectionWithoutChangingItsPermutation() {
        val order = PlaybackOrder(5, 0, PlaybackMode.Shuffle)
        val permutation = round(order, 5)
        permutation.forEachIndexed { position, index ->
            val next = permutation[(position + 1) % permutation.size]
            val previous = permutation[(position - 1 + permutation.size) % permutation.size]
            order.select(index)
            assertEquals(next, order.step())
            assertEquals(next, order.index)
            assertEquals(index, order.previous())
            assertEquals(previous, order.previous())
            assertEquals(index, order.next())
        }
    }

    @Test fun repeatOneReplaysCurrentSongAndSharesShufflesManualNavigation() {
        val order = PlaybackOrder(5, 2, PlaybackMode.Shuffle)
        val permutation = round(order, 5)
        order.mode = PlaybackMode.RepeatOne
        repeat(3) { assertEquals(2, order.step()) }
        assertEquals(permutation[1], order.next())
        assertEquals(permutation[1], order.step())
        assertEquals(2, order.previous())
        order.mode = PlaybackMode.Shuffle
        assertEquals(permutation[1], order.step())
    }

    @Test fun modeChangesKeepCurrentSongAndNavigationStartsFromTheLatestSelection() {
        val order = PlaybackOrder(5, 3, PlaybackMode.Shuffle)
        val permutation = round(order, 5)
        PlaybackMode.entries.forEach {
            order.mode = it
            assertEquals(3, order.index)
        }
        order.mode = PlaybackMode.Cyclic
        order.select(1)
        assertEquals(2, order.next())
        order.mode = PlaybackMode.Shuffle
        assertEquals(2, order.index)
        val position = permutation.indexOf(2)
        assertEquals(permutation[(position + 1) % permutation.size], order.next())
        assertEquals(2, order.previous())
        order.mode = PlaybackMode.Cyclic
        assertEquals(1, order.previous())
    }

    @Test fun repeatedlySwitchingModesDoesNotReshuffle() {
        val order = PlaybackOrder(5, 0, PlaybackMode.Shuffle)
        val permutation = round(order, 5)
        permutation.forEachIndexed { position, index ->
            order.select(index)
            repeat(3) {
                PlaybackMode.entries.forEach { mode ->
                    order.mode = mode
                    assertEquals(index, order.index)
                }
            }
            order.mode = PlaybackMode.Shuffle
            assertEquals(permutation[(position + 1) % permutation.size], order.next())
        }
    }

    @Test fun everyModeSupportsASingleSong() {
        val order = PlaybackOrder(1, 0)
        PlaybackMode.entries.forEach { mode ->
            order.mode = mode
            repeat(3) {
                assertEquals(0, order.step())
                assertEquals(0, order.next())
                assertEquals(0, order.previous())
            }
        }
    }

    @Test fun invalidQueuesAndSelectionsAreRejectedWithoutChangingCurrentSong() {
        assertThrows(IllegalArgumentException::class.java) { PlaybackOrder(0, 0) }
        assertThrows(IllegalArgumentException::class.java) { PlaybackOrder(-1, 0) }
        assertThrows(IllegalArgumentException::class.java) { PlaybackOrder(3, -1) }
        assertThrows(IllegalArgumentException::class.java) { PlaybackOrder(3, 3) }
        val order = PlaybackOrder(3, 1)
        assertThrows(IllegalArgumentException::class.java) { order.select(-1) }
        assertThrows(IllegalArgumentException::class.java) { order.select(3) }
        assertEquals(1, order.index)
        assertEquals(2, order.next())
    }

    @Test fun queuesHaveIndependentPositionsModesAndPermutations() {
        val first = PlaybackOrder(5, 2, PlaybackMode.Shuffle)
        val permutation = round(first, 5)
        val second = PlaybackOrder(2, 0)
        assertEquals(1, second.next())
        assertEquals(2, first.index)
        assertEquals(PlaybackMode.Shuffle, first.mode)
        assertEquals(permutation[1], first.next())
        assertEquals(1, second.index)
        assertEquals(PlaybackMode.Cyclic, second.mode)
        second.mode = PlaybackMode.RepeatOne
        second.select(0)
        assertEquals(permutation[1], first.index)
    }
}
