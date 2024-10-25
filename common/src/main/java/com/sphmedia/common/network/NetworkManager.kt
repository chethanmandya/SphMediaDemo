package com.byjus.common.network


import com.sphmedia.common.MockInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject

class NetworkManager @Inject constructor(
    private val fakeInterceptor: MockInterceptor
) {

    fun getClient(): OkHttpClient {
        val client = OkHttpClient.Builder().build()
        val builder = client.newBuilder()

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)
//        if (BuildConfig.DEBUG) {
//            builder.addNetworkInterceptor(fakeInterceptor)
//        }
        return builder.build()
    }
}