package top.stellortus.stellar_music.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

abstract class Preferences(private val name: String) {
    @Volatile
    private var storage: SharedPreferences? = null

    protected val prefs: SharedPreferences
        get() = checkNotNull(storage) {
            "Preferences '$name' is not initialized. Call init(context) before accessing it."
        }

    @Synchronized
    fun init(context: Context) {
        if (storage == null) {
            storage = context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
        }
    }

    fun clear() {
        prefs.edit { clear() }
    }
}
