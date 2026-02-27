package hu.kektura.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import hu.kektura.app.data.SettingsStore
import hu.kektura.app.data.TrailType
import hu.kektura.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selected = SettingsStore.getSelectedTrails(requireContext())
        binding.checkboxOkt.isChecked          = TrailType.OKT in selected
        binding.checkboxDelDunantuli.isChecked  = TrailType.DEL_DUNANTULI in selected
        binding.checkboxAlfoldi.isChecked       = TrailType.ALFOLDI in selected

        val checkboxes = listOf(
            binding.checkboxOkt          to TrailType.OKT,
            binding.checkboxDelDunantuli to TrailType.DEL_DUNANTULI,
            binding.checkboxAlfoldi      to TrailType.ALFOLDI
        )

        checkboxes.forEach { (checkbox, trail) ->
            checkbox.setOnCheckedChangeListener { _, _ ->
                val current = checkboxes
                    .filter { (cb, _) -> cb.isChecked }
                    .map { (_, t) -> t }
                    .toSet()

                if (current.isEmpty()) {
                    // Revert: at least one must remain selected
                    checkbox.isChecked = true
                    Toast.makeText(
                        requireContext(),
                        "Legalább egy túraútvonalat válassz ki.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    SettingsStore.setSelectedTrails(requireContext(), current)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
