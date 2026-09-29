package top.stellortus.stellarmusic.network

import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttpClient
import top.stellortus.stellarmusic.data.AuthPreferences

/**
 * 音频流播放所用的数据源工厂。
 *
 * 播放由 ExoPlayer 自行发起请求，不经过 Ktor 客户端，因此不会带上
 * [attachBearerToken] 附加的请求头，需要在此单独处理，否则服务端会以 401 拒绝。
 *
 * token 在每次请求发出时才从 [AuthPreferences] 读取，登录/登出后立即生效，
 * 不依赖数据源的创建时机。
 */
object AuthStreamDataSource {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token = AuthPreferences.current?.token
            val request = if (token.isNullOrEmpty()) {
                chain.request()
            } else {
                chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            }
            chain.proceed(request)
        }
        .build()

    val factory: OkHttpDataSource.Factory = OkHttpDataSource.Factory(client)
}
