package com.example.revoluttask.network

import com.example.revoluttask.BuildConfig
import com.example.revoluttask.network.latestrate.LatestRatesJsonResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.SocketTimeoutException
import java.net.UnknownHostException

enum class RevolutApiStatus { LOADING, ERROR, DONE, DONE_WITHOUT_CONNECTION }

private const val API_BASE_URL = "https://revolut.duckdns.org/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val client = OkHttpClient().newBuilder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    })
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(API_BASE_URL)
    .client(client)
    .build()

interface RevolutApiService {

    @GET("latest")
    fun getLatestRates(@Query("base") currency: String): Deferred<LatestRatesJsonResponse>
}

object RevolutApi {
    val retrofitService : RevolutApiService by lazy { retrofit.create(RevolutApiService::class.java) }

    class APIError(error: Throwable) {
        var message = "An error occurred"
        init {
            if (error is HttpException) {
                val errorJsonString = error.response()?.errorBody()?.string() ?: ""
                this.message = moshi.adapter(Error::class.java).fromJson(errorJsonString)?.error ?: this.message
            } else if(error is UnknownHostException) {
                this.message = "Cannot connect to the server."
            } else if(error is SocketTimeoutException) {
                this.message = "Request timed out."
            } else {
                this.message = error.message ?: this.message
            }
        }
    }
}

