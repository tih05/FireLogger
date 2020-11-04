package com.tikhomirov.fire_logger

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tikhomirov.fire_logger.repository.DeviceIdRepository
import com.tikhomirov.fire_logger.utils.NotificationHelper

internal class FireLoggerActivity : AppCompatActivity() {
    private val btnChange by lazy { findViewById<Button>(R.id.btnChange) }
    private val etDeviceId by lazy { findViewById<EditText>(R.id.etDeviceId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fire_logger)
        setupEditText()
        setupChangeButton()
    }

    private fun setupEditText() {
        etDeviceId.setText(DeviceIdRepository.getDeviceId())
    }

    private fun setupChangeButton() {
        btnChange.setOnClickListener {
            DeviceIdRepository.setDeviceId(etDeviceId.text.toString())
            NotificationHelper.updateNotificationTitle()
            finish()
        }
    }
}