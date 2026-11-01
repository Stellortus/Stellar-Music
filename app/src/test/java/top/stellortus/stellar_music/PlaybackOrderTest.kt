package top.stellortus.stellar_music

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import top.stellortus.stellar_music.ui.player.PlaybackOrder
import top.stellortus.stellar_music.ui.player.RepeatOneOrder
import top.stellortus.stellar_music.ui.player.SequentialOrder
import top.stellortus.stellar_music.ui.player.ShuffleOrder

class PlaybackOrderTest {
    private val orders: List<PlaybackOrder> = listOf(SequentialOrder, ShuffleOrder, RepeatOneOrder)
    private val permutation = intArrayOf(0, 3, 2, 1, 4)

    @Before fun initialize() {
        SequentialOrder.initialize(permutation.size)
        ShuffleOrder.initialize(permutation)
        RepeatOneOrder.initialize(permutation)
        commit(0)
    }

    @After fun reset() {
        orders.forEach { it.reset() }
    }

    private fun commit(index: Int?) {
        assertNotNull(index)
        orders.forEach { it.syncIndex(index!!) }
    }

    @Test fun sequentialOrderCalculatesWithoutMovingUntilCommittedAndWrapsBothWays() {
        assertEquals(1, SequentialOrder.next())
        assertEquals(1, SequentialOrder.step())
        assertEquals(0, SequentialOrder.index)
        commit(SequentialOrder.previous())
        assertEquals(4, SequentialOrder.index)
        commit(SequentialOrder.step())
        assertEquals(0, SequentialOrder.index)
    }

    @Test fun fixedShuffleIsReversibleFromEverySongIncludingAcrossBoundaries() {
        permutation.forEachIndexed { position, index ->
            commit(index)
            val next = permutation[(position + 1) % permutation.size]
            val previous = permutation[(position - 1 + permutation.size) % permutation.size]
            assertEquals(next, ShuffleOrder.step())
            assertEquals(next, ShuffleOrder.next())
            assertEquals(previous, ShuffleOrder.previous())
            commit(ShuffleOrder.next())
            assertEquals(index, ShuffleOrder.previous())
        }
    }

    @Test fun repeatedShuffleRoundsReuseTheSamePermutation() {
        val visited = mutableListOf<Int>()
        repeat(permutation.size * 3) {
            visited += ShuffleOrder.index
            commit(ShuffleOrder.step())
        }
        assertEquals(List(3) { permutation.toList() }.flatten(), visited)
    }

    @Test fun repeatOneKeepsCurrentSongOnCompletionButNavigatesTheFixedShuffleManually() {
        commit(2)
        repeat(3) { commit(RepeatOneOrder.step()) }
        assertTrue(orders.all { it.index == 2 })
        commit(RepeatOneOrder.next())
        assertTrue(orders.all { it.index == 1 })
        commit(RepeatOneOrder.previous())
        assertTrue(orders.all { it.index == 2 })
    }

    @Test fun manualSelectionSynchronizesOriginalIndicesWithoutChangingShuffle() {
        commit(1)
        assertTrue(orders.all { it.index == 1 })
        assertEquals(2, SequentialOrder.next())
        assertEquals(4, ShuffleOrder.next())
        assertEquals(4, RepeatOneOrder.next())
        assertEquals(1, RepeatOneOrder.step())
    }

    @Test fun strategiesKeepIndependentPositionsUntilTheControllerSynchronizesThem() {
        ShuffleOrder.syncIndex(3)
        assertEquals(0, SequentialOrder.index)
        assertEquals(0, RepeatOneOrder.step())
        assertEquals(2, ShuffleOrder.next())

        commit(3)
        assertTrue(orders.all { it.index == 3 })
        assertEquals(4, SequentialOrder.next())
        assertEquals(2, ShuffleOrder.next())
        assertEquals(2, RepeatOneOrder.next())
    }

    @Test fun permutationsAreCopiedAndReplacingAQueueDiscardsOldPositions() {
        val input = permutation.copyOf()
        ShuffleOrder.initialize(input)
        RepeatOneOrder.initialize(input)
        commit(0)
        input.fill(0)
        assertEquals(3, ShuffleOrder.next())
        assertEquals(3, RepeatOneOrder.next())

        SequentialOrder.initialize(2)
        ShuffleOrder.initialize(intArrayOf(1, 0))
        RepeatOneOrder.initialize(intArrayOf(1, 0))
        orders.forEach {
            assertEquals(-1, it.index)
            assertNull(it.step())
            assertNull(it.next())
            assertNull(it.previous())
        }
        commit(1)
        assertTrue(orders.all { it.next() == 0 && it.previous() == 0 })
        assertEquals(1, RepeatOneOrder.step())
    }

    @Test fun singletonQueuesRepeatAndResetMakesAllOperationsEmpty() {
        SequentialOrder.initialize(1)
        ShuffleOrder.initialize(intArrayOf(0))
        RepeatOneOrder.initialize(intArrayOf(0))
        commit(0)
        orders.forEach {
            assertEquals(0, it.step())
            assertEquals(0, it.next())
            assertEquals(0, it.previous())
            it.reset()
            assertEquals(-1, it.index)
            assertNull(it.step())
            assertNull(it.next())
            assertNull(it.previous())
        }
    }
}
