package top.stellortus.stellar_music.ui.player

/** Calculates original queue indices; the controller commits and synchronizes the result. */
internal abstract class PlaybackOrder {
    abstract val index: Int

    abstract fun step(): Int?
    abstract fun next(): Int?
    abstract fun previous(): Int?
    abstract fun syncIndex(index: Int)
    abstract fun reset()
}

internal object SequentialOrder : PlaybackOrder() {
    override var index: Int = -1
        private set
    private var size = 0

    fun initialize(size: Int) {
        require(size >= 0)
        this.size = size
        index = -1
    }

    override fun step(): Int? = next()

    override fun next(): Int? = if (index < 0) null else (index + 1) % size

    override fun previous(): Int? = if (index < 0) null else (index - 1 + size) % size

    override fun syncIndex(index: Int) {
        require(index in 0 until size)
        this.index = index
    }

    override fun reset() = initialize(0)
}

/** Inherited fields belong to each strategy instance, keeping their positions and arrays separate. */
internal abstract class PermutationPlaybackOrder : PlaybackOrder() {
    final override var index: Int = -1
        private set
    private var order = intArrayOf()

    /** The controller supplies a permutation containing each original queue index exactly once. */
    fun initialize(order: IntArray) {
        this.order = order.copyOf()
        index = -1
    }

    override fun step(): Int? = next()

    override fun next(): Int? = neighbor(1)

    override fun previous(): Int? = neighbor(-1)

    override fun syncIndex(index: Int) {
        require(index in order.indices)
        this.index = index
    }

    override fun reset() {
        index = -1
        order = intArrayOf()
    }

    private fun neighbor(direction: Int): Int? {
        val position = order.indexOf(index)
        if (position < 0) return null
        return order[(position + direction + order.size) % order.size]
    }
}

internal object ShuffleOrder : PermutationPlaybackOrder()

internal object RepeatOneOrder : PermutationPlaybackOrder() {
    override fun step(): Int? = index.takeIf { it >= 0 }
}
