package top.stellortus.stellarmusic.data

import android.content.Context
import android.content.SharedPreferences

/**
 * 登录态的本地存储，封装 SharedPreferences 的读写。
 *
 * 仅存储 token、userId、username 三个键，不存储密码。
 * 注意：token 目前使用普通 SharedPreferences 明文存储，生产环境可考虑改用
 * EncryptedSharedPreferences 或系统 Keystore 加密存储。
 */
class AuthPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var userId: Int?
        get() = prefs.getInt(KEY_USER_ID, -1).takeIf { it != -1 }
        set(value) {
            val editor = prefs.edit()
            if (value == null) editor.remove(KEY_USER_ID) else editor.putInt(KEY_USER_ID, value)
            editor.apply()
        }

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    /** 本地是否已有 token（不代表已通过服务器校验）。 */
    val hasToken: Boolean get() = !token.isNullOrEmpty()

    /** 登出时清空本地登录态。 */
    fun clear() {
        prefs.edit().clear().apply()
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

        /** 供网络层在无 Context 环境下读取 token（使用前需先调用 [getInstance] 初始化）。 */
        internal val current: AuthPreferences?
            get() = instance
    }
}
