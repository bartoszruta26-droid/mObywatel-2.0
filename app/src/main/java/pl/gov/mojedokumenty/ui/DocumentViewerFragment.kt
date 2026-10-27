package pl.gov.mojedokumenty.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import pl.gov.mojedokumenty.R
import pl.gov.mojedokumenty.data.DocumentRepository

/**
 * Podgląd jednego dokumentu.
 * SZKIELET: pokazuje metadane + miejsce na render PDF/zdjęcia.
 * Prawdziwy renderer (PDFView / PhotoView / WebView dla HTML) — zadania w README.
 */
class DocumentViewerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_document_viewer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val id = arguments?.getString("id")
        val doc = id?.let { DocumentRepository.getById(it) }

        view.findViewById<android.widget.TextView>(R.id.textDocTitle).text =
            doc?.title ?: getString(R.string.viewer_from_intent)
        view.findViewById<android.widget.TextView>(R.id.textDocMeta).text =
            doc?.let { "${it.type.displayName} • ${it.serialNumber}\nWydany: ${it.issueDate} • Ważny do: ${it.validUntil}" }
                ?: "Dokument zewnętrzny"

        // TODO (README): osadzić widok PDF tutaj, w ramce documentFrame
    }
}
