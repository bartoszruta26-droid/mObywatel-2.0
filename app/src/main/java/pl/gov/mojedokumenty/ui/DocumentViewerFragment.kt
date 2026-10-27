package pl.gov.mojedokumenty.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.gov.mojedokumenty.R
import pl.gov.mojedokumenty.data.DocumentFileResolver
import pl.gov.mojedokumenty.data.DocumentRepository
import pl.gov.mojedokumenty.databinding.FragmentDocumentViewerBinding

/**
 * Podgląd jednego dokumentu: renderer PDF (android.graphics.pdf.PdfRenderer —
 * bez zależności zewnętrznych) oraz obsługa obrazów PNG/JPG.
 * Zoom pinch zapewnia ScaleImageView.
 */
class DocumentViewerFragment : Fragment() {

    private var _binding: FragmentDocumentViewerBinding? = null
    private val binding get() = _binding!!

    private var renderer: PdfRenderer? = null
    private var pageCount = 0
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val repo = DocumentRepository.get(requireContext())
        val id = arguments?.getString(ARG_ID)
        val directUri = arguments?.getString(ARG_URI)

        val doc = id?.let { repo.getById(it) }

        binding.textDocTitle.text = doc?.title
            ?: getString(R.string.viewer_from_intent)

        binding.textDocMeta.text = doc?.let {
            "${it.type.displayName} • ${it.serialNumber}\n" +
                "Wydany: ${it.issueDate} • Ważny do: ${it.validUntil} • ${it.status.displayName}"
        } ?: getString(R.string.viewer_external_file)

        val location: String? = doc?.fileLocation ?: directUri
        val source = doc?.source
            ?: pl.gov.mojedokumenty.model.DocumentSource.USER_FILE

        if (location.isNullOrBlank() || source == pl.gov.mojedokumenty.model.DocumentSource.NONE) {
            showPlaceholder(getString(R.string.viewer_no_file))
            return
        }

        loadDocument(location, source)

        binding.buttonPrevPage.setOnClickListener { setPage(currentPage - 1) }
        binding.buttonNextPage.setOnClickListener { setPage(currentPage + 1) }
        binding.sliderPage.addOnChangeListener { _, value, fromUser ->
            if (fromUser) setPage(value.toInt())
        }
    }

    private fun loadDocument(location: String, source: pl.gov.mojedokumenty.model.DocumentSource) {
        showPlaceholder(getString(R.string.viewer_loading))
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val renderable = withContext(Dispatchers.IO) {
                    DocumentFileResolver.open(requireContext().applicationContext, location, source)
                }
                when (renderable) {
                    is DocumentFileResolver.Renderable.Image -> {
                        hideChrome()
                        binding.imagePage.setImageBitmap(renderable.bitmap)
                        binding.textPlaceholder.visibility = View.GONE
                    }

                    is DocumentFileResolver.Renderable.Pdf -> {
                        renderer = withContext(Dispatchers.IO) {
                            DocumentFileResolver.openPdfRenderer(renderable.descriptor)
                        }
                        pageCount = renderer!!.pageCount
                        currentPage = 0
                        binding.pageChrome.visibility = View.VISIBLE
                        binding.sliderPage.valueFrom = 0f
                        binding.sliderPage.valueTo = (pageCount - 1).coerceAtLeast(0).toFloat()
                        binding.sliderPage.stepSize = 1f
                        renderPage(currentPage)
                    }
                }
            } catch (t: Throwable) {
                showPlaceholder(getString(R.string.viewer_error, t.message ?: "?"))
            }
        }
    }

    private fun setPage(page: Int) {
        if (page < 0 || page >= pageCount) return
        currentPage = page
        renderPage(page)
    }

    private fun renderPage(page: Int) {
        val r = renderer ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                r.openPage(page).use { pdfPage ->
                    // Render w stałej rozdzielczości pionowej — czytelne na telefonie.
                    val height = RENDER_HEIGHT_PX
                    val width = (pdfPage.width * (height.toFloat() / pdfPage.height)).toInt()
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(Color.WHITE)
                    pdfPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bmp
                }
            }
            _binding?.let { b ->
                b.imagePage.setImageBitmap(bitmap)
                b.textPlaceholder.visibility = View.GONE
                b.textPageCounter.text =
                    getString(R.string.viewer_page_counter, page + 1, pageCount)
                b.sliderPage.setValues(floatArrayOf(page.toFloat()))
            }
        }
    }

    private fun showPlaceholder(text: String) {
        binding.textPlaceholder.text = text
        binding.textPlaceholder.visibility = View.VISIBLE
        binding.imagePage.setImageDrawable(null)
    }

    private fun hideChrome() {
        binding.pageChrome.visibility = View.GONE
        binding.textPlaceholder.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        runCatching { renderer?.close() }
        renderer = null
        _binding = null
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_URI = "uri"
        private const val RENDER_HEIGHT_PX = 1654 // ~A4 przy 200 dpi
    }
}
