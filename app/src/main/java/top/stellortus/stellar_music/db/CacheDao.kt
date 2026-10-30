package top.stellortus.stellar_music.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.logging.Level
import java.util.logging.Logger

@Dao
interface CacheDao {
    @Query("SELECT * FROM cache WHERE `key` = :key")
    suspend fun get(key: String): CacheEntity?

    @Upsert
    suspend fun save(entry: CacheEntity)

    @Query("DELETE FROM cache WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM cache")
    suspend fun clear()
}

/** A cache hit returns its snapshot without waiting for the session-owned synchronization. */
internal suspend fun CacheDao.readCached(
    key: String,
    scope: CoroutineScope,
    pending: MutableMap<String, Deferred<Unit>>,
    mutex: Mutex,
    fetch: suspend () -> String,
): String {
    scope.coroutineContext.ensureActive()
    val cached = get(key)
    scope.coroutineContext.ensureActive()
    val synchronization = synchronized(pending) {
        pending[key] ?: scope.async(start = CoroutineStart.LAZY) {
            mutex.withLock {
                // Another cache-miss caller may already have populated this key.
                if (cached == null && get(key) != null) return@withLock
                val json = fetch()
                currentCoroutineContext().ensureActive()
                save(CacheEntity(key, json))
            }
        }.also { task ->
            pending[key] = task
            task.invokeOnCompletion { failure ->
                synchronized(pending) {
                    if (pending[key] === task) pending.remove(key)
                }
                if (failure != null && failure !is CancellationException) {
                    Logger.getLogger("MusicCache").log(Level.WARNING, "Cache synchronization failed: $key", failure)
                }
            }
        }
    }
    synchronization.start()
    if (cached != null) {
        scope.coroutineContext.ensureActive()
        return cached.json
    }
    synchronization.await()
    scope.coroutineContext.ensureActive()
    val saved = get(key)
    scope.coroutineContext.ensureActive()
    return checkNotNull(saved) { "缓存写入失败：$key" }.json
}
