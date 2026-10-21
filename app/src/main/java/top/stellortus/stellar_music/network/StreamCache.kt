package top.stellortus.stellar_music.network

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@UnstableApi
object StreamCache {

    // Storage Limit
    private const val MAX_BYTES = 512L * 1024 * 1024

    private var cache: SimpleCache? = null

    fun dataSourceFactory(context: Context): DataSource.Factory {
        val appContext = context.applicationContext
        val simpleCache = cache ?: SimpleCache(
            File(appContext.cacheDir, "track_cache"),
            LeastRecentlyUsedCacheEvictor(MAX_BYTES),
            StandaloneDatabaseProvider(appContext),
        ).also { cache = it }

        return CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(AuthStreamDataSource.factory)
            .setCacheWriteDataSinkFactory(CacheDataSink.Factory().setCache(simpleCache))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}
