package com.tikhomirov.fire_logger

import android.annotation.SuppressLint
import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.lang.RuntimeException

@SuppressLint("SimpleDateFormat")

class FireLogger() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(request)
    }

    class Builder {

        fun build() = FireLogger()

        fun setContext(context: Context): Builder {
            /*no-op*/
            return this
        }

        fun setDirectory(directory: String): Builder {
            /*no-op*/
            return this
        }

        fun setIsEnabled(isEnabled: Boolean): Builder {
            /*no-op*/
            return this
        }

        fun replaceValue(value: String, replaceWith: String): Builder {
            /*no-op*/
            return this
        }
    }
}