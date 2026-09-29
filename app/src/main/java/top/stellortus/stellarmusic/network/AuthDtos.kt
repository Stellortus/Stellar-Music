package top.stellortus.stellarmusic.network

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val username: String, val password: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class UserDto(val id: Int, val username: String)

@Serializable
data class AuthResponse(val token: String, val user: UserDto)

@Serializable
data class ErrorResponse(val message: String)

@Serializable
data class MessageResponse(val message: String)
