package com.example.antitheft.services


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.antitheft.R
import com.example.antitheft.reciever.AlarmResetReceiver

class ChargerRemovalService : Service() {

    private lateinit var batteryReceiver: BroadcastReceiver
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var alarmResetReceiver: AlarmResetReceiver
    private lateinit var screenUnlockReceiver: ScreenUnlockReceiver

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Charger Removal Detection Service")
            .setContentText("Service is running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        startForeground(FOREGROUND_SERVICE_ID, notification)

        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI)

        alarmResetReceiver = AlarmResetReceiver(mediaPlayer)
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(alarmResetReceiver, filter)

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val status = intent.getIntExtra("plugged", -1)
                if (status == 0) { // Disconnected from power source
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                    }
                }
            }
        }
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, batteryFilter)

        screenUnlockReceiver = ScreenUnlockReceiver()
        val screenUnlockFilter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(screenUnlockReceiver, screenUnlockFilter)

        Log.d("ChargerRemovalService", "Service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        unregisterReceiver(alarmResetReceiver)
        unregisterReceiver(screenUnlockReceiver)
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        Log.d("ChargerRemovalService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Charger Removal Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "ChargerRemovalServiceChannel"
        const val FOREGROUND_SERVICE_ID = 2
    }

    inner class ScreenUnlockReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Stop the alarm when the screen is unlocked
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
            }
        }
    }
}

