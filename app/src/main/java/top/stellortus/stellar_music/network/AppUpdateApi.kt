package top.stellortus.stellar_music.network

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.expectSuccess
import top.stellortus.stellar_music.latestVersionNameUrl
import top.stellortus.stellar_music.network.NetworkService.client
import top.stellortus.stellar_music.utils.plugins.withoutAuth
import top.stellortus.stellar_music.versionCode

object AppUpdateApi {
    suspend fun latestVersionCode() =
        client.get(versionCode()) {
            withoutAuth()
            expectSuccess = true
        }.bodyAsText().trim().toIntOrNull()

    suspend fun latestVersionName(): String {
        val name = client.get(latestVersionNameUrl()) {
            withoutAuth()
            expectSuccess = true
        }.bodyAsText().trim()
        require(Regex("[a-zA-Z0-9][a-zA-Z0-9._+-]*").matches(name)) {
            "Update server returned an empty or invalid version name"
        }
        return name
    }
}
