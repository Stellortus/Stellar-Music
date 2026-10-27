package top.stellortus.stellar_music.utils.plugins

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.util.AttributeKey
import top.stellortus.stellar_music.data.AuthPreferences

private val SkipAuth = AttributeKey<Boolean>("SkipAuth")

fun HttpRequestBuilder.withoutAuth() {
    attributes.put(SkipAuth, true)
}

val TokenAuth = createClientPlugin("TokenAuth") {
    onRequest { request, _ ->
        if (request.attributes.getOrNull(SkipAuth) != true) {
            AuthPreferences.token?.let { token ->
                if (!request.headers.contains(HttpHeaders.Authorization)) {
                    request.headers.append(
                        HttpHeaders.Authorization,
                        "Bearer $token"
                    )
                }
            }
        }
    }
}
