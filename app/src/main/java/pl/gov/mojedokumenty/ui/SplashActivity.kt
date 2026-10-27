package pl.gov.mojedokumenty.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import pl.gov.mojedokumenty.MainActivity
import pl.gov.mojedokumenty.R

/**
 * Ekran powitalny z godłem państwa (godlo_orla.xml) — animacja wjazdu herbu.
 * Docelowo: SplashScreen API (Android 12+) — zadanie M6 w README.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<android.view.View>(R.id.splashEmblem)
            .startAnimation(AnimationUtils.loadAnimation(this, R.anim.splash_emblem_in))

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_DELAY_MS)
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1600L
    }
}
