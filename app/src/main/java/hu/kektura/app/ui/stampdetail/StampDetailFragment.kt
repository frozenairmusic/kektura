package hu.kektura.app.ui.stampdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import hu.kektura.app.databinding.FragmentStampDetailBinding

class StampDetailFragment : Fragment() {

    private var _binding: FragmentStampDetailBinding? = null
    private val binding get() = _binding!!

    private val segmentId: Int by lazy { requireArguments().getInt("segmentId") }

    private val viewModel: StampDetailViewModel by viewModels {
        StampDetailViewModel.Factory(requireActivity().application, segmentId)
    }

    private lateinit var adapter: StampGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStampDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StampGroupAdapter(
            onToggle  = { key -> viewModel.toggleGroup(key) },
            onCollect = { key -> viewModel.collectGroup(key) },
            onRemove  = { key -> viewModel.removeGroup(key) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.groupedList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

            val headers = list.filterIsInstance<StampListItem.GroupHeader>()
            val total     = headers.size
            val collected = headers.count { it.group.allCollected }
            binding.tvProgress.text      = "OKT-%02d  –  $collected / $total bélyegző".format(segmentId)
            binding.progressBar.max      = if (total > 0) total else 1
            binding.progressBar.progress = collected
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
