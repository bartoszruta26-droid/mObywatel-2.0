package pl.gov.mojedokumenty.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pl.gov.mojedokumenty.R

/** Ekran rejestru zdarzeń (historia wyświetleń dokumentów) — na razie placeholder. */
class RegisterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_placeholder, container, false)
}

/** Profil obywatela z danymi wzorowymi — placeholder. */
class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_placeholder, container, false)
}
