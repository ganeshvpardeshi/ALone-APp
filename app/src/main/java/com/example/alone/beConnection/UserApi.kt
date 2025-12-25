package com.example.alone.beConnection

import com.example.alone.model.LoginWithEmailRequest
import com.example.alone.model.LoginWithUsernameRequest
import com.example.alone.model.User
import com.example.alone.model.UserEmailUsername
import retrofit2.http.*

interface UserApi {
//    @POST("api/users")
//    suspend fun createUser(@Body user: User): User

    @POST("api/loginwithemail")
    suspend fun login_with_email(@Body user: LoginWithEmailRequest): User

    @POST("api/loginwithusername")
    suspend fun login_with_username(@Body user: LoginWithUsernameRequest): User

    @POST("api/register")
    suspend fun createUser(@Body user: User): User


//    get methods
    @GET("api/checkuser")
    suspend fun check_user_exists(@Query("email") email: String? = null, @Query("username") username: String? = null): UserEmailUsername

//    )
//    @GET("api/users/{unique_id}")
//    suspend fun getUser(@Path("unique_id") uniqueId: String): User
//
//    @GET("api/users/username/{username}")
//    suspend fun getUserByUsername(@Path("username") username: String): User







}
