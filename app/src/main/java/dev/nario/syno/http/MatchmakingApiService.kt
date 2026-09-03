package dev.nario.syno.http

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MatchmakingApiService {
    @POST("/matchmaking")
    fun findNearbyPlayer(@Body data: PlayerMatchmakingData): Call<FindNearbyPlayerResponse>

    @POST("/matchmaking/register")
    fun registerInMatchmaking(@Body data: PlayerMatchmakingData): Call<RegisterUserResponse>

    @GET("/matchmaking/{firebase_id}/match")
    fun getPlayerMatch(@Path("firebase_id") firebaseId: String): Call<FindNearbyPlayerResponse>

    @DELETE("/matchmaking/{firebase_id}")
    fun cancelMatchmaking(@Path("firebase_id") firebaseId: String): Call<Void>
}
