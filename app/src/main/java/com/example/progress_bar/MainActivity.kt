package com.example.progress_bar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {
    private val PROGRESS_MAX = 100
    private var PROGRESS_NOW = 0
    private val NOTIFICATION_ID = 167
    private val CHANNEL_NAME = "video upload"
    private val CHANNEL_ID = "upload channel"
    private val REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create notification channel
        createNotificationChannel()

        val build = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle("Video upload")
            setContentText("Uploading video...")
            setSmallIcon(R.drawable.notify)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Set sound for initial notification
            priority = NotificationCompat.PRIORITY_LOW
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE
                )
                return@setOnClickListener
            }

            // Notify for the first time to start with sound
            NotificationManagerCompat.from(this).apply {
                notify(NOTIFICATION_ID, build.build())
            }

            Thread {
                for (i in 1..1000) {
                    PROGRESS_NOW = i % PROGRESS_MAX
                    build.setProgress(PROGRESS_MAX, PROGRESS_NOW, false)

                    // Update the notification progress without resetting the sound
                    NotificationManagerCompat.from(this@MainActivity).apply {
                        notify(NOTIFICATION_ID, build.build())
                    }
                    Thread.sleep(100) // Simulate progress

                    // Check if progress has reached the maximum
                    if (PROGRESS_NOW == PROGRESS_MAX - 1) {
                        // Automatically close the notification after it reaches full
                        NotificationManagerCompat.from(this@MainActivity).apply {
                            cancel(NOTIFICATION_ID)
                        }
                        break // Exit the loop after closing the notification
                    }
                }
            }.start()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_NAME
            val descriptionText = "Video Uploading"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Set notification sound for the channel
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findViewById<Button>(R.id.button).performClick() // Retry button click after permission is granted
            }
        }
    }
}
