package com.example.shoe_store.service

import com.example.shoe_store.models.BasketItem
import com.example.shoe_store.models.Product
import com.example.shoe_store.models.RegistrationResponse
import com.example.shoe_store.models.User
import com.example.shoe_store.models.UserAuth
import com.example.shoe_store.models.UserRegistration
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    @POST("/v1/register")
    suspend fun register(@Body user: UserRegistration): Response<RegistrationResponse>

    @POST("/v1/token")
    suspend fun login(@Body authData: UserAuth): Response<User>

    @GET("/v1/")
    suspend fun getProducts(): Response<List<Product>>

    @GET("/v1/users/{user_id}")
    suspend fun getUserById(@Path("user_id") userId: Int): Response<User>

    @GET("/v1/{product_id}")
    suspend fun getProductById(@Path("product_id") productId: Int): Response<Product>

    @PUT("/v1/users/{user_id}")
    suspend fun updateUserWithoutPhoto(
        @Path("user_id") userId: Int,
        @Query("Login") login: String,
        @Query("Password") password: String,
        @Query("Name") name: String,
        @Query("Surname") surname: String,
        @Query("Patronymic") patronymic: String?
    ): Response<User>

    @Multipart
    @PUT("/v1/users/{user_id}")
    suspend fun updateUserWithPhoto(
        @Path("user_id") userId: Int,
        @Query("Login") login: String,
        @Query("Password") password: String,
        @Query("Name") name: String,
        @Query("Surname") surname: String,
        @Query("Patronymic") patronymic: String?,
        @Part photoFile: MultipartBody.Part
    ): Response<User>


    @GET("/v1/basket/get_all_basket")
    suspend fun getBasket(@Query("user_id") userId: Int): Response<List<BasketItem>>

    @POST("/v1/basket/add_to_basket")
    suspend fun addToBasket(
        @Query("user_id") userId: Int,
        @Query("product_id") productId: Int
    ): Response<Unit>

    @DELETE("/v1/basket/delete_from_basket")
    suspend fun deleteFromBasket(
        @Query("user_id") userId: Int,
        @Query("product_id") productId: Int
    ): Response<Unit>
}

object RetrofitClient {
    private const val BASE_URL = "http://212.20.53.169:1211/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}