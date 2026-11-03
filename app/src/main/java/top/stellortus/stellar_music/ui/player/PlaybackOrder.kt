package top.stellortus.stellar_music.ui.player

import top.stellortus.smooth_kotlin.selectors.CyclicSelector
import top.stellortus.smooth_kotlin.selectors.Selector
import top.stellortus.smooth_kotlin.selectors.ShuffledSelector

enum class PlaybackMode { Cyclic, Shuffle, RepeatOne }

/** Owns the position and selectors for one non-empty playback queue. */
internal class PlaybackOrder(size: Int, startIndex: Int, var mode: PlaybackMode = PlaybackMode.Cyclic) {
    init {
        require(size > 0)
        require(startIndex in 0 until size)
    }

    var index: Int = startIndex
        private set

    private val cyclic = CyclicSelector((0 until size).toList())
    private val shuffled = ShuffledSelector(cyclic.values)

    fun select(index: Int) {
        require(index in cyclic.values.indices)
        this.index = index
    }

    fun next(): Int = move(forward = true)

    fun previous(): Int = move(forward = false)

    fun step(): Int = if (mode == PlaybackMode.RepeatOne) index else next()

    private fun move(forward: Boolean): Int {
        val selector: Selector<Int> = if (mode == PlaybackMode.Cyclic) cyclic else shuffled
        selector.index = if (mode == PlaybackMode.Cyclic) index else shuffled.order.indexOf(index)
        if (forward) selector.stepForward() else selector.stepBackward()
        val selected by selector
        index = selected
        return index
    }
}
