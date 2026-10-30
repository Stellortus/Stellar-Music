package top.stellortus.stellar_music.db

import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CacheReadTest {
    private class MemoryDao : CacheDao {
        val entries = mutableMapOf<String, CacheEntity>()
        var saveGate: CompletableDeferred<Unit>? = null
        var savedValue: String? = null
        var saveFailure: Exception? = null
        var saves = 0

        override suspend fun get(key: String) = entries[key]

        override suspend fun save(entry: CacheEntity) {
            saveGate?.await()
            saveFailure?.let { throw it }
            entries[entry.key] = entry.copy(json = savedValue ?: entry.json)
            saves++
        }

        override suspend fun delete(key: String) { entries.remove(key) }
        override suspend fun clear() { entries.clear() }
    }

    private class Fixture {
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.Unconfined)
        val pending = mutableMapOf<String, Deferred<Unit>>()
        val mutex = Mutex()
        val dao = MemoryDao()

        suspend fun read(fetch: suspend () -> String) = dao.readCached("list", scope, pending, mutex, fetch)
        suspend fun finishSync() = job.children.toList().joinAll()
    }

    private suspend fun withCache(block: suspend Fixture.() -> Unit) {
        val fixture = Fixture()
        try {
            fixture.block()
        } finally {
            fixture.job.cancelAndJoin()
        }
    }

    @Test
    fun cacheHitReturnsBeforeNetworkAndNextCallSeesSynchronizedValue() = runBlocking {
        withCache {
            dao.entries["list"] = CacheEntity("list", "old")
            val response = CompletableDeferred<String>()

            val first = read { response.await() }
            assertEquals("old", first)
            assertFalse(response.isCompleted)

            response.complete("new")
            finishSync()
            assertEquals("new", dao.entries["list"]?.json)
            assertEquals("old", first)
            assertEquals("new", read { "newer" })
        }
    }

    @Test
    fun cacheMissWaitsForSaveAndReturnsDatabaseValue() = runBlocking {
        withCache {
            dao.saveGate = CompletableDeferred()
            dao.savedValue = "database-value"
            val caller = async { read { "network-value" } }
            yield()

            assertFalse(caller.isCompleted)
            assertTrue(dao.entries.isEmpty())
            dao.saveGate!!.complete(Unit)

            assertEquals("database-value", caller.await())
            assertEquals(1, dao.saves)
        }
    }

    @Test
    fun cachedEmptyListIsAHit() = runBlocking {
        withCache {
            dao.entries["list"] = CacheEntity("list", "[]")
            val response = CompletableDeferred<String>()
            assertEquals("[]", read { response.await() })
            response.complete("[1]")
            finishSync()
            assertEquals("[1]", dao.entries["list"]?.json)
        }
    }

    @Test
    fun failedBackgroundSyncPreservesCache() = runBlocking {
        withCache {
            dao.entries["list"] = CacheEntity("list", "old")
            assertEquals("old", read { throw IOException("offline") })
            finishSync()
            assertEquals("old", dao.entries["list"]?.json)
            assertEquals(0, dao.saves)
        }
    }

    @Test
    fun failedCacheMissPropagatesErrorWithoutSaving() = runBlocking {
        withCache {
            val failure = runCatching { read { throw IOException("offline") } }.exceptionOrNull()
            assertTrue(failure is IOException)
            assertTrue(dao.entries.isEmpty())
        }
    }

    @Test
    fun failedSaveDoesNotReturnNetworkValue() = runBlocking {
        withCache {
            dao.saveFailure = IOException("disk full")
            val failure = runCatching { read { "network-value" } }.exceptionOrNull()
            assertTrue(failure is IOException)
            assertTrue(dao.entries.isEmpty())
        }
    }

    @Test
    fun concurrentCacheMissesShareOneRequest() = runBlocking {
        withCache {
            val response = CompletableDeferred<String>()
            var fetches = 0
            val first = async { read { fetches++; response.await() } }
            yield()
            val second = async { read { fetches++; response.await() } }
            yield()

            assertEquals(1, fetches)
            response.complete("saved")
            assertEquals("saved", first.await())
            assertEquals("saved", second.await())
            assertEquals(1, dao.saves)
        }
    }

    @Test
    fun concurrentCacheHitsShareBackgroundRequest() = runBlocking {
        withCache {
            dao.entries["list"] = CacheEntity("list", "old")
            val response = CompletableDeferred<String>()
            var fetches = 0
            assertEquals("old", read { fetches++; response.await() })
            assertEquals("old", read { fetches++; response.await() })
            assertEquals(1, fetches)
            response.complete("new")
            finishSync()
            assertEquals(1, dao.saves)
        }
    }

    @Test
    fun leavingPageDoesNotCancelSynchronization() = runBlocking {
        withCache {
            val response = CompletableDeferred<String>()
            val caller = async { read { response.await() } }
            yield()
            caller.cancelAndJoin()

            assertTrue(job.isActive)
            response.complete("saved-after-page-exit")
            finishSync()
            assertEquals("saved-after-page-exit", dao.entries["list"]?.json)
        }
    }

    @Test
    fun sessionCleanupWaitsForRequestsAndRejectsLateResponse() = runBlocking {
        withCache {
            dao.entries["list"] = CacheEntity("list", "old")
            val response = CompletableDeferred<String>()
            assertEquals("old", read { withContext(NonCancellable) { response.await() } })

            job.cancel()
            val cleanup = async { job.join(); dao.clear() }
            yield()
            assertFalse(cleanup.isCompleted)

            response.complete("late-response")
            cleanup.await()
            assertTrue(dao.entries.isEmpty())
            assertEquals(0, dao.saves)
        }
    }
}
