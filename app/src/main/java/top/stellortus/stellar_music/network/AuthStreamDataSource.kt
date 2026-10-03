package top.stellortus.stellar_music.network

import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttpClient
import top.stellortus.stellar_music.data.AuthPreferences

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
