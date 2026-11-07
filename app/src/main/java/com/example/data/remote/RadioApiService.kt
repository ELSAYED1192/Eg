package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class RadioBrowserStationDto(
    @Json(name = "stationuuid") val stationUuid: String,
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String,
    @Json(name = "url_resolved") val urlResolved: String?,
    @Json(name = "country") val country: String?,
    @Json(name = "countrycode") val countryCode: String?,
    @Json(name = "tags") val tags: String?,
    @Json(name = "bitrate") val bitrate: Int? = 128,
    @Json(name = "codec") val codec: String? = null
)

interface RadioApiService {

    @GET("json/stations/search")
    suspend fun searchStationsByLanguage(
        @Query("language") language: String = "arabic",
        @Query("limit") limit: Int = 40,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true
    ): List<RadioBrowserStationDto>

    @GET("json/stations/bytag/classic")
    suspend fun getClassicStations(
        @Query("limit") limit: Int = 15
    ): List<RadioBrowserStationDto>

    @GET("json/stations/bytag/sport")
    suspend fun getSportStations(
        @Query("limit") limit: Int = 15
    ): List<RadioBrowserStationDto>

    companion object {
        private const val BASE_URL = "https://de1.api.radio-browser.info/"

        fun create(): RadioApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(RadioApiService::class.java)
        }
    }
}
