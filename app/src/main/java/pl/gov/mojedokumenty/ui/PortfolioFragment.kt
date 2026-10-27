package pl.gov.mojedokumenty.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.gov.mojedokumenty.R
import pl.gov.mojedokumenty.data.DocumentRepository
import pl.gov.mojedokumenty.databinding.FragmentPortfolioBinding
import pl.gov.mojedokumenty.databinding.ListItemDocumentBinding

/**
 * „Portfel dokumentów” — lista dokumentów podanych do aplikacji.
 * Kliknięcie karty otwiera Podgląd dokumentu (DocumentViewerFragment).
 */
class PortfolioFragment : Fragment() {

    private var _binding: FragmentPortfolioBinding? = null
    private val binding get() = _binding!!

    private val adapter = DocumentAdapter { doc ->
        val args = Bundle().apply { putString("id", doc.id) }
        findNavController().navigate(R.id.action_portfolio_to_viewer, args)
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                DocumentRepository.documents.collectLatest { list ->
                    adapter.submit(list)
                    binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Prosty adapter szkieleatu — ListAdapter/DiffUtil jako zadanie w README. */
    class DocumentAdapter(
        private val onClick: (pl.gov.mojedokumenty.model.GovernmentDocument) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<DocumentAdapter.VH>() {

        private var items: List<pl.gov.mojedokumenty.model.GovernmentDocument> = emptyList()

        fun submit(list: List<pl.gov.mojedokumenty.model.GovernmentDocument>) {
            items = list
            notifyDataSetChanged()
        }

        inner class VH(val b: ListItemDocumentBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ListItemDocumentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return VH(b)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val doc = items[position]
            holder.b.textTitle.text = doc.title
            holder.b.textSerial.text = doc.serialNumber
            holder.b.textStatus.text = doc.status.displayName
            holder.b.textType.text = doc.type.displayName
            holder.b.root.setOnClickListener { onClick(doc) }
        }
    }
}
