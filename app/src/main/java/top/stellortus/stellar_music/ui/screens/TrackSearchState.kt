package top.stellortus.stellar_music.ui.screens

import androidx.compose.runtime.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import top.stellortus.stellar_music.network.NetworkService
import top.stellortus.stellar_music_common.dto.Track

class TrackSearchState(
    private val scope: CoroutineScope,
    private val search: suspend (String) -> List<Track> = NetworkService::searchTracks
) {
    var query by mutableStateOf(""); private set
    var results by mutableStateOf<List<Track>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var submitted by mutableStateOf(false); private set
    var failed by mutableStateOf(false); private set
    private var request: Job? = null
    private var revision = 0

    fun edit(value: String) {
        cancel()
        query = value
        results = emptyList()
        loading = false
        submitted = false
        failed = false
    }

    fun cancel() {
        revision++
        request?.cancel()
        loading = false
    }

    fun submit(onError: (Throwable) -> Unit) {
        val key = query.trim()
        edit(query)
        if (key.isEmpty()) return
        val currentRevision = revision
        loading = true
        submitted = true
        request = scope.launch {
            try {
                val response = search(key)
                if (revision == currentRevision) results = response
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                if (revision == currentRevision) {
                    failed = true
                    onError(error)
                }
            } finally {
                if (revision == currentRevision) loading = false
            }
        }
    }
}

