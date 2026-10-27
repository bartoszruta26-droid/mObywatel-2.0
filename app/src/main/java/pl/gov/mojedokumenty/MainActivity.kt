package pl.gov.mojedokumenty

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import pl.gov.mojedokumenty.databinding.ActivityMainBinding

/**
 * Główna aktywność aplikacji „Moje Dokumenty”.
 * Dolna nawigacja: Portfel / Rejestr / Profil. Odbiera pliki PDF/obrazy
 * udostępnione z innych aplikacji (ACTION_VIEW) i przekazuje je do portfela.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.bottomNav
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        handleViewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleViewIntent(intent)
    }

    /** Dokument przesłany intentem → import do portfela (użytkownik decyduje o trwałości). */
    private fun handleViewIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri = intent.data!!
            val bundle = Bundle().apply { putString("uri", uri.toString()) }
            findNavController(R.id.nav_host_fragment)
                .navigate(R.id.action_global_documentViewer, bundle)
        }
    }
}
