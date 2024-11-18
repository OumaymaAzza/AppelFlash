package com.example.appelflash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Lier les vues
        val imageView = findViewById<ImageView>(R.id.ImageView)
        val textView = findViewById<TextView>(R.id.text1)

        // Ajouter des animations
        val animation = AnimationUtils.loadAnimation(this, R.anim.slide)
        imageView.startAnimation(animation)
        textView.startAnimation(animation)

        // Vérifier les permissions et les demander si nécessaire
        if (allPermissionsGranted()) {
            // Lancer l'activité après le délai
            startNextActivity()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        Toast.makeText(this, "Application démarrée", Toast.LENGTH_SHORT).show()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startNextActivity() {
        // Démarrer FlashActivity après 3 secondes
        Handler().postDelayed({
            val intent = Intent(this, FlashActivity::class.java)
            startActivity(intent)
            finish()  // Terminer MainActivity pour qu'elle ne reste pas dans la pile d'activités
        }, 3000) // Délai de 3000 millisecondes (3 secondes)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isEmpty() || grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Les permissions sont nécessaires pour que l'application fonctionne.", Toast.LENGTH_LONG).show()
            } else {
                // Si les permissions sont accordées, lancer l'activité suivante
                startNextActivity()
            }
        }
    }
}
