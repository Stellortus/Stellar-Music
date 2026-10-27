package top.stellortus.stellar_music.data

import androidx.core.content.edit

object AuthPreferences : Preferences("auth_prefs") {
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

    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "userId"
    private const val KEY_USERNAME = "username"
}
