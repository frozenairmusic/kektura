package hu.kektura.app.ui.stamps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import hu.kektura.app.R
import hu.kektura.app.data.SettingsStore
import hu.kektura.app.data.TrailType
import hu.kektura.app.databinding.FragmentStampsBinding

class StampsFragment : Fragment() {

    private var _binding: FragmentStampsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StampsViewModel by viewModels()

    private val adapter = SegmentRowAdapter { row ->
        val args = Bundle().apply { putInt("segmentId", row.segment.id) }
        findNavController().navigate(R.id.action_stamps_to_stampDetail, args)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStampsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        refreshContent()
    }

    private fun refreshContent() {
        val selectedTrails = SettingsStore.getSelectedTrails(requireContext())
        val hasOkt = selectedTrails.contains(TrailType.OKT)

        if (hasOkt) {
            binding.recyclerView.visibility  = View.VISIBLE
            binding.comingSoonText.visibility = View.GONE

            viewModel.segmentRows.observe(viewLifecycleOwner) { rows ->
                adapter.submitList(rows)
                val total = rows.sumOf { it.stampCount }
                val collected = rows.sumOf { it.collectedCount }
                binding.tvProgress.text      = "$collected / $total bélyegző"
                binding.progressBar.max      = if (total > 0) total else 1
                binding.progressBar.progress = collected
            }
        } else {
            binding.recyclerView.visibility  = View.GONE
            binding.comingSoonText.visibility = View.VISIBLE
            val names = selectedTrails.joinToString(", ") { it.displayName }
            binding.comingSoonText.text = "$names – hamarosan elérhető."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
