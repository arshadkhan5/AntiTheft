package com.example.antitheft.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import com.example.antitheft.services.ChargerRemovalService
import com.example.antitheft.services.MotionDetectionService
import com.example.antitheft.services.PocketRemovalService

class AlarmResetReceiver(private val mediaPlayer: MediaPlayer) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_USER_PRESENT == intent?.action) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }

            if (intent.action == Intent.ACTION_USER_PRESENT) {
                context?.let {
                    stopAlarmServices(it)
                }
            }
        }
    }

    private fun stopAlarmServices(context: Context) {
        // Stop the services
        context.stopService(Intent(context, PocketRemovalService::class.java))
        context.stopService(Intent(context, ChargerRemovalService::class.java))
        context.stopService(Intent(context, MotionDetectionService::class.java))
    }

}