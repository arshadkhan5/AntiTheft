package com.example.antitheft.services



import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.provider.Settings

import androidx.core.app.NotificationCompat
import com.example.antitheft.R
import com.example.antitheft.reciever.AlarmResetReceiver
import com.example.antitheft.view.MainActivity


class PocketRemovalService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var proximitySensor: Sensor
    private lateinit var mediaPlayer: MediaPlayer

    private var isPocketRemoved = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainActivityPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pocket Removal Service")
            .setContentText("Pocket Removal  Service is running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(R.drawable.stop, "Stop", mainActivityPendingIntent)
            .build()
        startForeground(FOREGROUND_SERVICE_ID, notification)

        mediaPlayer = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!!

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)

        Log.d("PocketRemovalService", "Service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        Log.d("PocketRemovalService", "Service destroyed")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH
            val gForce = kotlin.math.sqrt(gX * gX + gY * gY + gZ * gZ)

            if (gForce > 2.5 && isPocketRemoved) { // Shake detected and pocket is removed
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
            }
        } else if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < proximitySensor.maximumRange) {
                // Object detected near proximity sensor (phone is out of pocket)
                isPocketRemoved = true
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Pocket Removal Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "PocketRemovalServiceChannel"
        const val FOREGROUND_SERVICE_ID = 1
        const val ACTION_STOP = "com.example.motiondetectionservice.ACTION_STOP"
    }
}

/*

class PocketRemovalService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var alarmResetReceiver: AlarmResetReceiver

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pocket Removal Detection Service")
            .setContentText("Pocket Removal Service is running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(FOREGROUND_SERVICE_ID, notification)

        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI)

        alarmResetReceiver = AlarmResetReceiver(mediaPlayer)
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(alarmResetReceiver, filter)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!!
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)

        Log.d("PocketRemovalService", "Service started")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_PROXIMITY) {
                val distance = it.values[0]
                if (distance < proximitySensor.maximumRange) {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(alarmResetReceiver)
        sensorManager.unregisterListener(this)
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        Log.d("PocketRemovalService", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Pocket Removal Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
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

    companion object {
        const val CHANNEL_ID = "PocketRemovalServiceChannel"
        const val FOREGROUND_SERVICE_ID = 1
    }
}*/
