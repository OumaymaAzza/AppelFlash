package com.example.appelflash

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class PhoneStateReceiver : BroadcastReceiver() {

    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var isFlashOn = false
    private val handler = Handler(Looper.getMainLooper())
    private var flashRunnable: Runnable? = null

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        // Initialiser la gestion de la caméra si ce n'est pas déjà fait
        if (!::cameraManager.isInitialized) {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                    val characteristics = cameraManager.getCameraCharacteristics(id)
                    characteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
                Log.e("PhoneStateReceiver", "Erreur lors de l'accès à la caméra")
            }
        }

        // Gérer les différents états du téléphone
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                Log.d("PhoneStateReceiver", "Appel entrant")
                Toast.makeText(context, "Appel entrant", Toast.LENGTH_SHORT).show()
                startFlashing(context)
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                Log.d("PhoneStateReceiver", "Appel en cours")
                Toast.makeText(context, "Appel en cours", Toast.LENGTH_SHORT).show()
                stopFlashing(context)
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.d("PhoneStateReceiver", "Appel terminé ou rejeté")
                Toast.makeText(context, "Appel terminé", Toast.LENGTH_SHORT).show()
                stopFlashing(context)
            }
        }
    }

    private fun startFlashing(context: Context) {
        flashRunnable = object : Runnable {
            override fun run() {
                try {
                    cameraId?.let { id ->
                        cameraManager.setTorchMode(id, !isFlashOn)
                        isFlashOn = !isFlashOn
                    }
                    handler.postDelayed(this, 500)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                    Log.e("PhoneStateReceiver", "Erreur lors du contrôle du flash")
                }
            }
        }

        // Démarrer immédiatement le clignotement
        handler.post(flashRunnable!!)
    }

    private fun stopFlashing(context: Context) {
        // Si un clignotement est en cours, l'arrêter
        flashRunnable?.let {
            handler.removeCallbacks(it)  // Retirer le Runnable
            flashRunnable = null  // Réinitialiser le Runnable
        }

        try {
            // Si le flash est allumé, éteindre le flash
            if (isFlashOn) {
                cameraId?.let { id ->
                    cameraManager.setTorchMode(id, false)  // Éteindre le flash
                    isFlashOn = false
                    Log.d("PhoneStateReceiver", "Flash éteint")
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e("PhoneStateReceiver", "Erreur lors de l'extinction du flash")
        }

        // Afficher un Snackbar si le contexte est une activité
        if (context is Activity) {
            Snackbar.make(
                context.findViewById(android.R.id.content),
                "Appel rejeté ou terminé, arrêt du flash",
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            Log.w("PhoneStateReceiver", "Contexte non Activité, Snackbar non affiché.")
        }
    }
}
