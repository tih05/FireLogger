package com.tikhomirov.fire_logger

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tikhomirov.fire_logger.repository.Constants
import com.tikhomirov.fire_logger.repository.DeviceIdRepository
import com.tikhomirov.fire_logger.utils.JSONUtils
import com.tikhomirov.fire_logger.utils.NotificationHelper
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

@SuppressLint("SimpleDateFormat")

class FireLogger() : Interceptor {

    companion object {
        private const val PREFIX_HTTPS = "https://"
        private const val PREFIX_HTTP = "http://"
        private const val REQUEST = "request"
        private const val RESPONSE = "response"
        private const val HEADERS = "Headers"
        private const val ENDPOINT = "Endpoint"
        private const val BODY = "Body"
        private const val URL = "URL"
        private const val METHOD = "Method"
        private const val QUERY_PARAMS = "Query params"
        private const val CODE = "Code"
        private const val MESSAGE = "Message"

        internal fun newInstance(
            context: Context,
            isEnabled: Boolean,
            directories: List<String>,
            valuesToReplace: Map<String, String>
        ): FireLogger {
            if (isEnabled) {
                DeviceIdRepository.init(context)
                NotificationHelper.init(context)
            }

            return FireLogger().apply {
                requestsRefs = directories.fold(requestsRefs) { ref, directory ->
                    ref.child(directory)
                }
                this.isEnabled = isEnabled
                this.valuesToReplace = valuesToReplace
            }
        }
    }

    private var isEnabled: Boolean = true
    private lateinit var valuesToReplace: Map<String, String>


    private val database = FirebaseDatabase.getInstance().reference
    private var requestsRefs = database.child("requests")
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
    private val timeFormatter: SimpleDateFormat = SimpleDateFormat("HH:mm:ss:SS")
    private val todayDate = dateFormatter.format(Date())
    private val sessionStartTime = timeFormatter.format(Date())


    override fun intercept(chain: Interceptor.Chain): Response {
        if (isEnabled.not()) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()

        val time = timeFormatter.format(Date())
        val endpoint = request
            .url().toString()
            .removePrefix(PREFIX_HTTPS)
            .removePrefix(PREFIX_HTTP)
            .removePrefix(request.url().host())
            .replace('/', '\\')

        val deviceRef =
            "$sessionStartTime - ${DeviceIdRepository.getSessionId()} ${DeviceIdRepository.getDeviceId()}"
        val ref = requestsRefs
            .child(todayDate)
            .child(deviceRef)
            .child("$time $endpoint")

        val response = try {
            chain.proceed(chain.request())
        } catch (e: Exception) {
            //TODO Create WorkManager and caching for sending errors
            Timer(Constants.TIMER_TAG, false)
                .schedule(5000L) {
                    ref.child("exception").setValue(e.toString())
                }
            throw e
        }

        saveRequest(ref, request)
        saveResponse(ref, response)

        return response
    }


    private fun saveRequest(ref: DatabaseReference, request: Request) {
        val requestRef = ref.child(REQUEST)

        requestRef.child(URL).setValue(request.url().toString())

        requestRef.child(ENDPOINT).setValue(
            request.url().toString()
                .removePrefix(PREFIX_HTTPS)
                .removePrefix(PREFIX_HTTP)
                .removePrefix(request.url().host())
        )

        requestRef.child(METHOD).setValue(request.method())

        request.url().queryParameterNames().forEach {
            requestRef
                .child(QUERY_PARAMS)
                .child(it)
                .setValue(request.url().queryParameter(it))
        }

        request.headers().names()
            .forEach {
                requestRef
                    .child(HEADERS)
                    .child(it)
                    .setValue(request.headers().values(it).toString())
            }

        request.body()?.let {
            requestRef
                .child(BODY)
                .setValue(request.getBodyAsObject())
        }

    }


    private fun saveResponse(ref: DatabaseReference, response: Response) {
        val responseRef = ref.child(RESPONSE)

        response.headers().names()
            .forEach {
                responseRef
                    .child(HEADERS)
                    .child(it)
                    .setValue(response.headers().values(it).toString())
            }

        responseRef
            .child(CODE)
            .setValue(response.code())

        var bytesAmount = response.body()?.contentLength() ?: 0
        if (bytesAmount == -1L) {
            bytesAmount = Long.MAX_VALUE
        }
        response.peekBody(bytesAmount).string().let {
            val obj = JSONUtils.getObject(it, valuesToReplace)
            responseRef
                .child(BODY)
                .setValue(obj)
        }

        val message = response.message().toString()
        if (message.isBlank()) {
            responseRef
                .child(MESSAGE)
                .setValue(message)
        }
    }

    private fun Request.getBodyAsObject(): Any? {
        val copy = this.newBuilder().build()
        return copy.body()?.let {
            val requestString = try {
                val buffer = Buffer()
                it.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
            requestString?.let {
                JSONUtils.getObject(it, valuesToReplace)
            }
        }
    }


    class Builder {
        private var context: Context? = null
        private var directories = mutableListOf<String>()
        private var isEnabled: Boolean = true
        private var valuesToReplace = mutableMapOf<String, String>()

        fun build(): FireLogger {
            return context?.let {
                FireLogger.newInstance(
                    context = it,
                    isEnabled = isEnabled,
                    directories = directories,
                    valuesToReplace = valuesToReplace
                )
            } ?: run {
                throw RuntimeException("Context must not be null! Please provide valid context!")
            }
        }

        fun setContext(context: Context): Builder {
            this.context = context
            return this
        }

        fun setDirectory(directory: String): Builder {
            this.directories.add(directory)
            return this
        }

        fun setIsEnabled(isEnabled: Boolean): Builder {
            this.isEnabled = isEnabled
            return this
        }

        fun replaceValue(value: String, replaceWith: String): Builder {
            this.valuesToReplace[value] = replaceWith
            return this
        }
    }

}