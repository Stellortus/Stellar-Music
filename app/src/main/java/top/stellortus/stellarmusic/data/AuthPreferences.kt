package top.stellortus.stellarmusic.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AuthPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit { putString(KEY_TOKEN, value) }

    var userId: Int?
        get() = prefs.getInt(KEY_USER_ID, -1).takeIf { it != -1 }
        set(value) {
            prefs.edit {
                if (value == null) remove(KEY_USER_ID) else putInt(KEY_USER_ID, value)
            }
        }

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit { putString(KEY_USERNAME, value) }

    val hasToken: Boolean get() = !token.isNullOrEmpty()

    fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USERNAME = "username"

        @Volatile
        private var instance: AuthPreferences? = null

        fun getInstance(context: Context): AuthPreferences =
            instance ?: synchronized(this) {
                instance ?: AuthPreferences(context).also { instance = it }
            }

        internal val current: AuthPreferences?
            get() = instance
    }
}
