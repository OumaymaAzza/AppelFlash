package com.example.appelflash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class FlashActivity : AppCompatActivity() {

    private lateinit var toggleFlashButton: Button
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var isFlashOn = false
    private lateinit var flashStateReceiver: BroadcastReceiver

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flash)

        // Initialisation des éléments
        toggleFlashButton = findViewById(R.id.toggleFlashButton)
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        // Trouver l'ID de la caméra arrière
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        // Récepteur pour l'état du flash
        flashStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val flashState = intent.getBooleanExtra("flash_state", false)
                if (flashState) {
                    turnOnFlash()
                } else {
                    turnOffFlash()
                }
            }
        }

        // Enregistrer le récepteur du flash
        val filter = IntentFilter("com.example.appelflash.FLASH_STATE_CHANGED")
        registerReceiver(flashStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

        // Action du bouton pour activer/désactiver le flash
        toggleFlashButton.setOnClickListener {
            if (isFlashOn) {
                turnOffFlash()
            } else {
                turnOnFlash()
            }
        }
    }

    // Fonction pour activer le flash
    private fun turnOnFlash() {
        try {
            cameraId?.let { id ->
                cameraManager.setTorchMode(id, true)
                isFlashOn = true
                toggleFlashButton.text = "Désactiver le flash"
                Toast.makeText(this, "Flash activé", Toast.LENGTH_SHORT).show()
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    // Fonction pour désactiver le flash
    private fun turnOffFlash() {
        try {
            cameraId?.let { id ->
                cameraManager.setTorchMode(id, false)
                isFlashOn = false
                toggleFlashButton.text = "Activer le flash"
                Toast.makeText(this, "Flash désactivé", Toast.LENGTH_SHORT).show()
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    // Désenregistrer le récepteur à la destruction de l'activité
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(flashStateReceiver)
    }
}
