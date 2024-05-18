package com.example.antitheft.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.antitheft.databinding.FragmentDashBoardBinding
import com.example.antitheft.services.ChargerRemovalService
import com.example.antitheft.services.MotionDetectionService
import com.example.antitheft.services.PocketRemovalService


class DashBoard : Fragment() {
    private var binding: FragmentDashBoardBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDashBoardBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.buttonPocketRemoval?.setOnClickListener {
            Log.e("myTag", "buttonPocketRemoval clickListener: ")
            val serviceIntent =
                Intent((context ?: return@setOnClickListener), PocketRemovalService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (activity ?: return@setOnClickListener).startForegroundService(serviceIntent)
            } else {
                (activity ?: return@setOnClickListener).startService(
                    Intent(
                        serviceIntent
                    )
                )

            }
        }


        binding?.buttonChargerRemoval?.setOnClickListener {
            Log.e("myTag", "buttonChargerRemoval clickListener: ")
            val serviceIntent =
                Intent((context ?: return@setOnClickListener), ChargerRemovalService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (activity ?: return@setOnClickListener).startForegroundService(serviceIntent)
            } else {
                (activity ?: return@setOnClickListener).startService(
                    Intent(
                        serviceIntent
                    )
                )

            }
        }


        binding?.buttonMotionDetection?.setOnClickListener {
            Log.e("myTag", "buttonMotionDetection clickListener: ")
            val serviceIntent = Intent((context ?: return@setOnClickListener), MotionDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (activity ?: return@setOnClickListener).startForegroundService(serviceIntent)
            } else {
                (activity ?: return@setOnClickListener).startService(
                    Intent(
                        serviceIntent
                    )
                )

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}