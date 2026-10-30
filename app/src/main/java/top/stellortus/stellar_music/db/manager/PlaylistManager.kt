package top.stellortus.stellar_music.db.manager

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import top.stellortus.stellar_music.db.CacheEntity
import top.stellortus.stellar_music.db.PlaylistsDatabase
import top.stellortus.stellar_music.db.readCached
import top.stellortus.stellar_music.network.PlaylistApi
import top.stellortus.stellar_music.network.networkJson
import top.stellortus.stellar_music_common.dto.CreatePlaylistRequest
import top.stellortus.stellar_music_common.dto.Playlist
import top.stellortus.stellar_music_common.dto.PlaylistDetail
import top.stellortus.stellar_music_common.dto.UpdatePlaylistRequest

object PlaylistManager {
    internal const val CACHE_UPDATE_FAILED = "操作成功，缓存更新失败，请重新打开页面"
    private lateinit var database: PlaylistsDatabase
    private val dao get() = database.cacheDao()
    @Volatile private var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pending = mutableMapOf<String, Deferred<Unit>>()
    private val mutationMutex = Mutex()

    @Synchronized
    fun init(context: Context) {
        if (!::database.isInitialized) {
            database = Room.databaseBuilder(context.applicationContext, PlaylistsDatabase::class.java, "playlists.db")
                .build()
        }
    }

    fun startSession() {
        if (!scope.isActive) scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    suspend fun listPlaylists(): List<Playlist> {
        val json = dao.readCached("list", scope, pending, mutationMutex) {
            networkJson.encodeToString(PlaylistApi.listPlaylists())
        }
        return networkJson.decodeFromString(json)
    }

    suspend fun getPlaylist(id: Int): PlaylistDetail {
        val json = dao.readCached("detail:$id", scope, pending, mutationMutex) {
            networkJson.encodeToString(PlaylistApi.getPlaylist(id))
        }
        return networkJson.decodeFromString(json)
    }

    suspend fun createPlaylist(request: CreatePlaylistRequest): Unit = change {
        PlaylistApi.createPlaylist(request)
        afterChange { saveList() }
    }

    suspend fun updatePlaylist(id: Int, request: UpdatePlaylistRequest): Unit = change {
        val update = if (request.public == null) {
            saveList()
            val summary = networkJson.decodeFromString<List<Playlist>>(checkNotNull(dao.get("list")).json)
                .find { it.id == id } ?: error("无法读取歌单公开状态，请稍后重试")
            request.copy(public = summary.public)
        } else request
        PlaylistApi.updatePlaylist(id, update)
        afterChange {
            saveList()
            if (dao.get("detail:$id") != null) saveDetail(id)
        }
    }

    suspend fun deletePlaylist(id: Int): Unit = change {
        PlaylistApi.deletePlaylist(id)
        afterChange {
            database.withTransaction {
                dao.delete("detail:$id")
                dao.get("list")?.let { cached ->
                    val remaining = networkJson.decodeFromString<List<Playlist>>(cached.json).filterNot { it.id == id }
                    dao.save(cached.copy(json = networkJson.encodeToString(remaining)))
                }
            }
            saveList()
        }
    }

    suspend fun addTracks(id: Int, trackIds: List<Int>): Unit = change {
        PlaylistApi.addTracks(id, trackIds)
        afterChange { saveDetail(id); saveList() }
    }

    suspend fun removeTrack(id: Int, trackId: Int): Unit = change {
        PlaylistApi.removeTrack(id, trackId)
        afterChange { saveDetail(id); saveList() }
    }

    private suspend fun saveList() {
        val playlists = PlaylistApi.listPlaylists()
        currentCoroutineContext().ensureActive()
        dao.save(CacheEntity("list", networkJson.encodeToString(playlists)))
    }

    private suspend fun saveDetail(id: Int) {
        val detail = PlaylistApi.getPlaylist(id)
        currentCoroutineContext().ensureActive()
        dao.save(CacheEntity("detail:$id", networkJson.encodeToString(detail)))
    }

    private suspend fun change(block: suspend () -> Unit) {
        scope.async { mutationMutex.withLock { block() } }.await()
    }

    private suspend fun afterChange(block: suspend () -> Unit) {
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            throw IllegalStateException(CACHE_UPDATE_FAILED, error)
        }
    }

    suspend fun clear() {
        scope.coroutineContext[Job]!!.cancelAndJoin()
        dao.clear()
    }
}
