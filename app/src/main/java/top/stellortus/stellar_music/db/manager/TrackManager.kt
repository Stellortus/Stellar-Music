package top.stellortus.stellar_music.db.manager

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.encodeToString
import top.stellortus.stellar_music.db.TracksDatabase
import top.stellortus.stellar_music.db.readCached
import top.stellortus.stellar_music.network.NetworkService
import top.stellortus.stellar_music.network.networkJson
import top.stellortus.stellar_music_common.dto.Track

object TrackManager {
    private lateinit var database: TracksDatabase
    @Volatile private var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pending = mutableMapOf<String, Deferred<Unit>>()
    private val mutex = Mutex()

    @Synchronized
    fun init(context: Context) {
        if (!::database.isInitialized) {
            database = Room.databaseBuilder(context.applicationContext, TracksDatabase::class.java, "tracks.db")
                .build()
        }
    }

    fun startSession() {
        if (!scope.isActive) scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    suspend fun getMusicList(start: Int = 0): List<Track> {
        require(start >= 0)
        val json = database.cacheDao().readCached("page:$start", scope, pending, mutex) {
            networkJson.encodeToString(NetworkService.getMusicList(start))
        }
        return networkJson.decodeFromString(json)
    }

    suspend fun getAllMusicList(maxTracks: Int = 500): List<Track> {
        val all = mutableListOf<Track>()
        var start = 0
        while (all.size < maxTracks) {
            val page = getMusicList(start)
            if (page.isEmpty()) break
            all += page
            start += page.size
        }
        return all.take(maxTracks)
    }

    suspend fun clear() {
        scope.coroutineContext[Job]!!.cancelAndJoin()
        database.cacheDao().clear()
    }
}
