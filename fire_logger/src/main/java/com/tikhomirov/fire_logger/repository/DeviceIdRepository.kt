package com.tikhomirov.fire_logger.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.tikhomirov.fire_logger.repository.Constants.ID_TAG

internal object DeviceIdRepository {
    private lateinit var sharedPreferences: SharedPreferences
    private var deviceId: String? = null
    private var sessionId: String? = null

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(
            Constants.FIRE_LOGGER_PREFS,
            Context.MODE_PRIVATE
        )
        initDeviceId()
    }

    fun getDeviceId() = deviceId!!

    fun setDeviceId(id: String) {
        sharedPreferences
            .edit()
            .putString(Constants.ID_TAG, id)
            .apply()
        deviceId = id
    }


    private fun initDeviceId() {
        deviceId = sharedPreferences.getString(ID_TAG, null)
        if (deviceId.isNullOrBlank()) {
            deviceId = "${Build.MANUFACTURER} ${Build.MODEL}"
        }
    }

    fun getSessionId(): String {
        if (sessionId.isNullOrBlank()) {
            val allowedChars = ('A'..'Z') + ('0'..'9')
            sessionId = (1..4)
                .map { allowedChars.random() }
                .joinToString("")
        }
        return sessionId!!
    }
}