package top.stellortus.stellar_music.network

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import top.stellortus.stellar_music.authLoginUrl
import top.stellortus.stellar_music.authLogoutUrl
import top.stellortus.stellar_music.authMeUrl
import top.stellortus.stellar_music.authRegisterUrl
import top.stellortus.stellar_music.utils.extensions.bodyOrThrow
import top.stellortus.stellar_music_common.dto.AuthResponse
import top.stellortus.stellar_music_common.dto.LoginRequest
import top.stellortus.stellar_music_common.dto.MessageResponse
import top.stellortus.stellar_music_common.dto.RegisterRequest
import top.stellortus.stellar_music_common.dto.User

object AuthApi {
    private val client get() = NetworkService.client

    suspend fun register(username: String, password: String): AuthResponse =
        client.post(authRegisterUrl()) {
            setBody(RegisterRequest(username, password))
        }.bodyOrThrow()

    suspend fun login(username: String, password: String): AuthResponse =
        client.post(authLoginUrl()) {
            setBody(LoginRequest(username, password))
        }.bodyOrThrow()

    suspend fun me(): User =
        client.get(authMeUrl()).bodyOrThrow()

    suspend fun logout(): MessageResponse =
        client.post(authLogoutUrl()).bodyOrThrow()
}

class ApiException(val status: Int, message: String) : Exception(message)
