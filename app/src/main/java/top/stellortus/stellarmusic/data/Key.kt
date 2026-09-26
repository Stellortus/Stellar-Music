package top.stellortus.stellarmusic.data

import android.provider.ContactsContract
import io.ktor.util.Platform
import top.stellortus.stellarmusic.lang.KeyName

sealed interface Key {
    val keyName: String
}
class DataKey(override val keyName: String) : Key {

    companion object {
        private val cache = mutableMapOf<String, DataKey>()

        private fun getOrPut(key: String, putBehavior: () -> DataKey = { DataKey(key) }) =
            cache.getOrPut(key) { putBehavior() }


    }
}

enum class NameKey: Key {
    UserName;
    override val keyName = this.name
}


