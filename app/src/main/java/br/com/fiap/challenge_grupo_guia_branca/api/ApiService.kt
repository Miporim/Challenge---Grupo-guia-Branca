package br.com.fiap.challenge_grupo_guia_branca.api

import br.com.fiap.challenge_grupo_guia_branca.dto.LoginDTO
import br.com.fiap.challenge_grupo_guia_branca.dto.RegisterUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginResponse(
    val token: String
)

data class MeResponse(
    val email: String
)

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginDTO
    ): LoginResponse

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterUser
    ): Response<Unit>

    @GET("api/auth/me")
    suspend fun me(): MeResponse
}