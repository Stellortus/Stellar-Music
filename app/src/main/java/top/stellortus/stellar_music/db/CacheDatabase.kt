package top.stellortus.stellar_music.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CacheEntity::class], version = 1, exportSchema = true)
abstract class TracksDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}

@Database(entities = [CacheEntity::class], version = 1, exportSchema = true)
abstract class PlaylistsDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
