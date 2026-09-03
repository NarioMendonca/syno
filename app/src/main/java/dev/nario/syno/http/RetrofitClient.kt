package dev.nario.syno.http

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // emulator uses 10.0.2.2 to access the pc localhost
    private const val BASE_URL = "http://10.0.2.2:3333/"

    private val okHttpClient = OkHttpClient.Builder()
        // matchmaking could take 20 seconds to respond
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    val matchmakingApi: MatchmakingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MatchmakingApiService::class.java)
    }
}
