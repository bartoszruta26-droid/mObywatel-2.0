package pl.gov.mojedokumenty.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.gov.mojedokumenty.R
import pl.gov.mojedokumenty.data.DocumentRepository
import pl.gov.mojedokumenty.databinding.FragmentPortfolioBinding
import pl.gov.mojedokumenty.databinding.ListItemDocumentBinding
import pl.gov.mojedokumenty.model.GovernmentDocument

/**
 * „Portfel dokumentów” — lista dokumentów podanych do aplikacji.
 * - przycisk „Dodaj dokument” otwiera wybór pliku (Storage Access Framework),
 * - wyszukiwarka filtruje po tytule/numerze seryjnym,
 * - kliknięcie karty otwiera Podgląd (DocumentViewerFragment).
 */
class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!

    private val adapter = DocumentAdapter { doc ->
        val args = Bundle().apply { putString("id", doc.id) }
        findNavController().navigate(R.id.action_portfolio_to_viewer, args)
    }

    private var allDocuments: List<GovernmentDocument> = emptyList()
    private var query: String = ""

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) importUri(uri, persistable = true)
    }

    private fun importUri(uri: Uri, persistable: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = DocumentRepository.get(requireContext())
                .importUserDocument(uri, persistable)
            result.fold(
                onSuccess = { Snackbar.make(binding.root, getString(R.string.import_success, it.title), Snackbar.LENGTH_LONG).show() },
                onFailure = { Snackbar.make(binding.root, getString(R.string.import_error, it.message ?: "?"), Snackbar.LENGTH_LONG).show() }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerDocuments.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDocuments.adapter = adapter

        binding.buttonAddDocument.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/pdf", "image/png", "image/jpeg"))
        }

        binding.searchDocuments.addTextChangedListener { text ->
            query = text?.toString()?.trim().orEmpty()
            applyFilter()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                DocumentRepository.get(requireContext()).documents.collectLatest { list ->
                    allDocuments = list
                    applyFilter()
                }
            }
        }
    }

    private fun applyFilter() {
        val filtered = if (query.isBlank()) allDocuments else allDocuments.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.serialNumber.contains(query, ignoreCase = true) ||
                it.type.displayName.contains(query, ignoreCase = true)
        }
        adapter.submit(filtered)
        binding.emptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.emptyState.text = if (allDocuments.isEmpty()) {
            getString(R.string.portfolio_empty)
        } else {
            getString(R.string.portfolio_no_results)
        }
    }

    /** Obsługa intentu ACTION_VIEW przesłanego do MainActivity. */
    fun handleIncomingUri(uri: Uri) {
        importUri(uri, persistable = false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Adapter kart dokumentów. */
    class DocumentAdapter(
        private val onClick: (GovernmentDocument) -> Unit
    ) : RecyclerView.Adapter<DocumentAdapter.VH>() {

        private var items: List<GovernmentDocument> = emptyList()

        fun submit(list: List<GovernmentDocument>) {
            items = list
            notifyDataSetChanged()
        }

        inner class VH(val b: ListItemDocumentBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ListItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val doc = items[position]
            holder.b.textTitle.text = doc.title
            holder.b.textSerial.text = doc.serialNumber
            holder.b.textStatus.text = doc.status.displayName
            holder.b.textType.text = doc.type.displayName.uppercase()
            holder.b.textValidity.text = holder.b.root.context.getString(
                R.string.portfolio_validity, doc.issueDate, doc.validUntil
            )
            holder.b.root.setOnClickListener { onClick(doc) }
        }
    }
}
