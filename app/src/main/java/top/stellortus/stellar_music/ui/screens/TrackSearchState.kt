package top.stellortus.stellar_music.ui.screens

import kotlinx.coroutines.CoroutineScope
import top.stellortus.stellar_music.network.NetworkService
import top.stellortus.stellar_music_common.dto.Track

class TrackSearchState(
    scope: CoroutineScope,
    search: suspend (String) -> List<Track> = NetworkService::searchTracks,
) : SearchState<Track>(scope, search)
