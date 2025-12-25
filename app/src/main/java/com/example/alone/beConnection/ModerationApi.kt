package com.example.alone.beConnection

import com.example.alone.model.ModerateRequest
import com.example.alone.model.ModerateResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ModerationApi {
    @POST("moderate")
    suspend fun moderate(@Body body: ModerateRequest): ModerateResponse
}
