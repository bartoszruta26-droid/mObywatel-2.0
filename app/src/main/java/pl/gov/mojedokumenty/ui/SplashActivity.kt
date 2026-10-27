package pl.gov.mojedokumenty.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import pl.gov.mojedokumenty.MainActivity
import pl.gov.mojedokumenty.R

/**
 * Ekran powitalny z godłem państwa (godlo_orla.xml).
 * Czas wyświetlania i animację docelowo przenosimy do SplashScreen API — zadanie w README.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_DELAY_MS)
    }

    companion object {
        private const val SPLASH_DELAY_MS = 1500L
    }
}
