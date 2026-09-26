package top.stellortus.stellarmusic.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

class ArgsPreferences(private val preferences: SharedPreferences) :
    SharedPreferences by preferences {
    fun read(key: Key): String? {
        return preferences.getString(key.keyName, null)
    }

    fun store(key: Key, value: Any) {
        preferences.edit(commit = true) {
            putString(key.keyName, value.toString())
        }
    }

    fun delete(key: Key) {
        preferences.edit(commit = true) {
            remove(key.keyName)
        }
    }

    fun exists(key: Key) = !read(key).isNullOrEmpty()

}

enum class PreferencesName {
    UserInfo
}

val preferencesMap = mutableMapOf<PreferencesName, ArgsPreferences>()

@Composable
fun preferences(name: PreferencesName) =
    preferencesMap.getOrPut(name) {
        val context = LocalContext.current
        val preferences = remember(context) {
            context.getSharedPreferences(name.name, Context.MODE_PRIVATE)
        }
        ArgsPreferences(preferences)
    }
