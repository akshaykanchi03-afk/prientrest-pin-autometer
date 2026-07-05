package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.*

@JsonClass(generateAdapter = true)
data class PinterestTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Int,
    @Json(name = "scope") val scope: String
)

@JsonClass(generateAdapter = true)
data class PinterestUserResponse(
    @Json(name = "username") val username: String,
    @Json(name = "profile_image") val profileImage: String? = null
)

@JsonClass(generateAdapter = true)
data class PinterestBoard(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null
)

@JsonClass(generateAdapter = true)
data class PinterestBoardsResponse(
    @Json(name = "items") val items: List<PinterestBoard>,
    @Json(name = "bookmark") val bookmark: String? = null
)

@JsonClass(generateAdapter = true)
data class MediaSource(
    @Json(name = "source_type") val sourceType: String = "image_url",
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class PinterestPinRequest(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "destination_link") val destinationLink: String?,
    @Json(name = "board_id") val boardId: String,
    @Json(name = "media_source") val mediaSource: MediaSource
)

@JsonClass(generateAdapter = true)
data class PinterestPinResponse(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String? = null
)

interface PinterestApi {

    @FormUrlEncoded
    @POST("v5/oauth/token")
    suspend fun exchangeToken(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): PinterestTokenResponse

    @GET("v5/user_account")
    suspend fun getUserProfile(
        @Header("Authorization") bearerToken: String
    ): PinterestUserResponse

    @GET("v5/boards")
    suspend fun getBoards(
        @Header("Authorization") bearerToken: String
    ): PinterestBoardsResponse

    @POST("v5/pins")
    suspend fun createPin(
        @Header("Authorization") bearerToken: String,
        @Body request: PinterestPinRequest
    ): PinterestPinResponse
}
