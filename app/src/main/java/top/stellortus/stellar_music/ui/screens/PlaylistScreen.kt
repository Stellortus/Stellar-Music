package top.stellortus.stellar_music.ui.screens

import androidx.annotation.OptIn
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import top.stellortus.stellar_music.ui.player.MusicPlayerState
import top.stellortus.stellar_music.ui.screens.playlist.PlaylistDetailScreen
import top.stellortus.stellar_music.ui.screens.playlist.PlaylistListScreen
import top.stellortus.stellar_music.ui.screens.playlist.PlaylistListState
import top.stellortus.stellar_music.ui.screens.playlist.PlaylistSearchScreen
import top.stellortus.stellar_music.ui.screens.playlist.PlaylistSearchState

const val ROUTE_PLAYLISTS = "playlists"
private const val ARG_PLAYLIST_ID = "id"
private const val ROUTE_PLAYLIST_DETAIL = "$ROUTE_PLAYLISTS/{$ARG_PLAYLIST_ID}"
private const val ROUTE_PLAYLIST_SEARCH = "$ROUTE_PLAYLISTS/search"

@OptIn(UnstableApi::class)
@Composable
fun PlaylistScreen(player: MusicPlayerState) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val searchState = remember { PlaylistSearchState(scope) }
    val searchListState = rememberLazyListState()
    val playlistListState = rememberLazyListState()
    val playlistPageState = remember { PlaylistListState() }

    NavHost(
        navController = navController,
        startDestination = ROUTE_PLAYLISTS,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        modifier = Modifier.fillMaxSize()
    ) {
        composable(
            route = ROUTE_PLAYLISTS,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
            sizeTransform = { null }
        ) {
            PlaylistListScreen(
                pageState = playlistPageState, listState = playlistListState,
                onPlayPlaylist = { player.togglePlaylist(it) },
                currentPlaylistId = player.currentPlaylistId,
                isPlaying = player.isPlaybackRequested,
                controlFeedbackVersion = player.playlistControlFeedbackVersion,
                onOpenPlaylist = { id ->
                    playlistPageState.loaded = false
                    navController.navigate("$ROUTE_PLAYLISTS/$id")
                },
                onSearch = { key ->
                    if (key.isNotBlank()) {
                        searchState.edit(key.trim())
                        scope.launch { searchListState.scrollToItem(0) }
                        navController.navigate(ROUTE_PLAYLIST_SEARCH) { launchSingleTop = true }
                    }
                }
            )
        }
        composable(
            route = ROUTE_PLAYLIST_SEARCH,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
            sizeTransform = { null }
        ) {
            PlaylistSearchScreen(searchState, searchListState,
                onPlayPlaylist = { player.togglePlaylist(it) },
                currentPlaylistId = player.currentPlaylistId,
                isPlaying = player.isPlaybackRequested,
                controlFeedbackVersion = player.playlistControlFeedbackVersion,
                onBack = { searchState.cancel(); navController.popBackStack() },
                onOpenPlaylist = { id -> navController.navigate("$ROUTE_PLAYLISTS/$id") })
        }
        composable(
            route = ROUTE_PLAYLIST_DETAIL,
            arguments = listOf(navArgument(ARG_PLAYLIST_ID) { type = NavType.IntType })
        ) { entry ->
            PlaylistDetailScreen(
                playlistId = entry.arguments?.getInt(ARG_PLAYLIST_ID) ?: 0,
                player = player,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
