package top.stellortus.stellar_music.ui.player

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import top.stellortus.stellar_music_common.dto.Track

/** Owns the playback snapshot and rejects responses from superseded selections. */
internal class PlaylistPlaybackController(
    private val scope: CoroutineScope,
    private val loadTracks: suspend (Int) -> List<Track>,
    private val onPlay: (Track) -> Unit,
    private val onError: (String) -> Unit,
    private val onQueueChanged: (List<Track>, Int) -> Unit = { _, _ -> },
    private val onSourcePlaylistChanged: (Int?) -> Unit = {},
    private val onTogglePause: () -> Unit = {},
) {
    var currentPlaylistId: Int? = null
        private set

    private var tracks = emptyList<Track>()
    private var loopEnabled = false
    private var request: Job? = null
    private var revision = 0
    // These stateful objects belong to the app's single playback session.
    private val orders = listOf(SequentialOrder, ShuffleOrder, RepeatOneOrder)
    private val activeOrder: PlaybackOrder = SequentialOrder

    init {
        orders.forEach { it.reset() }
    }

    /** Home selections are temporary, single-track playlists using the same playback pipeline. */
    fun play(track: Track) = playPlaylist(
        tracks = listOf(track),
        startIndex = 0,
        sourcePlaylistId = null,
    )

    fun playPlaylist(playlistId: Int) {
        cancelRequest()
        val selection = revision
        request = scope.launch {
            try {
                val loaded = loadTracks(playlistId)
                if (selection == revision) startPlaylist(loaded, 0, playlistId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (selection == revision) onError("歌单加载失败：${error.message ?: "请稍后重试"}")
            }
        }
    }

    fun playPlaylist(tracks: List<Track>, startIndex: Int = 0, sourcePlaylistId: Int? = null) {
        cancelRequest()
        startPlaylist(tracks, startIndex, sourcePlaylistId)
    }

    fun togglePlaylist(playlistId: Int, snapshot: List<Track>? = null) {
        if (currentPlaylistId == playlistId) {
            cancelRequest()
            onTogglePause()
        } else if (snapshot != null) {
            playPlaylist(snapshot, sourcePlaylistId = playlistId)
        } else {
            playPlaylist(playlistId)
        }
    }

    private fun updateSource(playlistId: Int?) {
        currentPlaylistId = playlistId
        onSourcePlaylistChanged(playlistId)
    }

    private fun startPlaylist(snapshot: List<Track>, startIndex: Int, sourcePlaylistId: Int?) {
        if (snapshot.isEmpty()) {
            onError("歌单里还没有歌曲")
            return
        }
        require(startIndex in snapshot.indices)
        tracks = snapshot.toList()
        SequentialOrder.initialize(tracks.size)
        val order = (listOf(startIndex) + tracks.indices.filter { it != startIndex }.shuffled()).toIntArray()
        ShuffleOrder.initialize(order)
        RepeatOneOrder.initialize(order)
        updateSource(sourcePlaylistId)
        playResolvedIndex(startIndex)
    }

    fun playQueueIndex(selectedIndex: Int) {
        if (selectedIndex !in tracks.indices) return
        cancelRequest()
        if (selectedIndex == activeOrder.index) return
        playResolvedIndex(selectedIndex)
    }

    fun next() {
        cancelRequest()
        playResolvedIndex(activeOrder.next())
    }

    fun previous() {
        cancelRequest()
        playResolvedIndex(activeOrder.previous())
    }

    fun onCompleted() {
        if (!loopEnabled || tracks.isEmpty()) return
        playResolvedIndex(activeOrder.step())
    }

    private fun playResolvedIndex(target: Int?) {
        if (target == null || target !in tracks.indices) return
        orders.forEach { it.syncIndex(target) }
        loopEnabled = true
        onQueueChanged(tracks, target)
        onPlay(tracks[target])
    }

    fun stopLoop() {
        loopEnabled = false
    }

    fun release() {
        cancelRequest()
        stopLoop()
        tracks = emptyList()
        currentPlaylistId = null
        orders.forEach { it.reset() }
    }

    fun cancelRequest() {
        revision++
        request?.cancel()
        request = null
    }
}
