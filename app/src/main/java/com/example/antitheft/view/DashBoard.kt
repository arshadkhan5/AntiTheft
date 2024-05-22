package com.example.antitheft.view

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashBoardBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("ServicePrefs", Context.MODE_PRIVATE)

        // Restore switch states from SharedPreferences
        binding?.switchPocketRemoval?.isChecked = sharedPreferences.getBoolean("PocketRemovalService", false)
        binding?.switchChargerRemoval?.isChecked = sharedPreferences.getBoolean("ChargerRemovalService", false)
        binding?.switchMotionDetection?.isChecked = sharedPreferences.getBoolean("MotionDetectionService", false)

        // Set listeners for switches
        binding?.switchPocketRemoval?.setOnCheckedChangeListener { _, isChecked ->
            val serviceIntent = Intent(context, PocketRemovalService::class.java)
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity?.startForegroundService(serviceIntent)
                } else {
                    activity?.startService(serviceIntent)
                }
                updateServiceStatus("PocketRemovalService", true)
            } else {
                activity?.stopService(serviceIntent)
                updateServiceStatus("PocketRemovalService", false)
            }
        }

        binding?.switchChargerRemoval?.setOnCheckedChangeListener { _, isChecked ->
            val serviceIntent = Intent(context, ChargerRemovalService::class.java)
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity?.startForegroundService(serviceIntent)
                } else {
                    activity?.startService(serviceIntent)
                }
                updateServiceStatus("ChargerRemovalService", true)
            } else {
                activity?.stopService(serviceIntent)
                updateServiceStatus("ChargerRemovalService", false)
            }
        }

        binding?.switchMotionDetection?.setOnCheckedChangeListener { _, isChecked ->
            val serviceIntent = Intent(context, MotionDetectionService::class.java)
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity?.startForegroundService(serviceIntent)
                } else {
                    activity?.startService(serviceIntent)
                }
                updateServiceStatus("MotionDetectionService", true)
            } else {
                activity?.stopService(serviceIntent)
                updateServiceStatus("MotionDetectionService", false)
            }
        }

        binding?.buttonStopPocketRemoval?.setOnClickListener {
            stopService(PocketRemovalService::class.java)
            binding?.switchPocketRemoval?.isChecked = false
        }

        binding?.buttonStopChargerRemoval?.setOnClickListener {
            stopService(ChargerRemovalService::class.java)
            binding?.switchChargerRemoval?.isChecked = false
        }

        binding?.buttonStopMotionDetection?.setOnClickListener {
            stopService(MotionDetectionService::class.java)
            binding?.switchMotionDetection?.isChecked = false
        }

        // Update UI based on the current state of the services
        updateServiceStatus("PocketRemovalService", isServiceRunning(PocketRemovalService::class.java))
        updateServiceStatus("ChargerRemovalService", isServiceRunning(ChargerRemovalService::class.java))
        updateServiceStatus("MotionDetectionService", isServiceRunning(MotionDetectionService::class.java))
    }

    private fun updateServiceStatus(service: String, isRunning: Boolean) {
        when (service) {
            "PocketRemovalService" -> {
                binding?.textPocketRemovalStatus?.text = if (isRunning) "Status: Running" else "Status: Not Running"
                binding?.buttonStopPocketRemoval?.visibility = if (isRunning) View.VISIBLE else View.GONE
                binding?.switchPocketRemoval?.isChecked = isRunning
            }
            "ChargerRemovalService" -> {
                binding?.textChargerRemovalStatus?.text = if (isRunning) "Status: Running" else "Status: Not Running"
                binding?.buttonStopChargerRemoval?.visibility = if (isRunning) View.VISIBLE else View.GONE
                binding?.switchChargerRemoval?.isChecked = isRunning
            }
            "MotionDetectionService" -> {
                binding?.textMotionDetectionStatus?.text = if (isRunning) "Status: Running" else "Status: Not Running"
                binding?.buttonStopMotionDetection?.visibility = if (isRunning) View.VISIBLE else View.GONE
                binding?.switchMotionDetection?.isChecked = isRunning
            }
        }
        sharedPreferences.edit().putBoolean(service, isRunning).apply()
    }

    private fun stopService(serviceClass: Class<*>) {
        activity?.stopService(Intent(context, serviceClass))
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = activity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}


