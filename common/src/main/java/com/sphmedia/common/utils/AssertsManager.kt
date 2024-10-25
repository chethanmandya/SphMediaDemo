package com.byjus.common.utils

import android.content.Context
import com.google.gson.Gson
import timber.log.Timber

object AssertsManager {

    fun openStringFile(fileName: String, context: Context): String {
        return try {
            val reader = context.assets.open(fileName).bufferedReader()

            reader.useLines { sequence: Sequence<String> ->
                sequence.reduce { a, b -> "$a\n$b" }
            }
        } catch (e: Exception) {
            Timber.e("AssertsManager - ${e.message}")
            ""
        }

    }

    fun <T> convertJsonStringToObject(jsonString: String, clazz: Class<T>): T =
        Gson().fromJson(jsonString, clazz)
}
