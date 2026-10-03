package top.stellortus.stellar_music.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import top.stellortus.stellar_music.ui.screens.playlist.PlaylistDetailScreen
import top.stellortus.stellar_music.ui.screens.playlist.PlaylistListScreen

const val ROUTE_PLAYLISTS = "playlists"
private const val ARG_PLAYLIST_ID = "id"
private const val ROUTE_PLAYLIST_DETAIL = "$ROUTE_PLAYLISTS/{$ARG_PLAYLIST_ID}"

@Composable
fun PlayListScreen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_PLAYLISTS,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(ROUTE_PLAYLISTS) {
            PlaylistListScreen(
                onOpenPlaylist = { id -> navController.navigate("$ROUTE_PLAYLISTS/$id") }
            )
        }
        composable(
            route = ROUTE_PLAYLIST_DETAIL,
            arguments = listOf(navArgument(ARG_PLAYLIST_ID) { type = NavType.IntType })
        ) { entry ->
            PlaylistDetailScreen(
                playlistId = entry.arguments?.getInt(ARG_PLAYLIST_ID) ?: 0,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
