package com.example.autotallerapp.di

import com.example.autotallerapp.BuildConfig
import com.example.autotallerapp.data.remote.ia.GeminiApi
import com.example.autotallerapp.data.remote.ia.GeminiConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/** Cliente HTTP compartido.*/
@Module
@InstallIn(SingletonComponent::class)
object RedModule {
    @Provides
    @Singleton
    fun proveerOkHttp(): OkHttpClient {
        val constructor = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)   // una respuesta multimodal puede tardar
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            // BASIC: solo metodo, URL y codigo. Nunca el cuerpo (llevaria las fotos en Base64)
            constructor.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader("x-goog-api-key")
                }
            )
        }
        return constructor.build()
    }

    @Provides
    @Singleton
    fun proveerGeminiApi(cliente: OkHttpClient): GeminiApi =
        Retrofit.Builder()
            .baseUrl(GeminiConfig.URL_BASE)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
}