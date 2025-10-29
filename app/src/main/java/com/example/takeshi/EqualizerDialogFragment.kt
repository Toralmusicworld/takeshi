package com.example.takeshi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.takeshi.data.AppDatabase
import com.example.takeshi.data.EqualizerSettings
import com.example.takeshi.databinding.DialogEqualizerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class EqualizerDialogFragment : DialogFragment() {

    private var _binding: DialogEqualizerBinding? = null
    private val binding get() = _binding!!
    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private val equalizerDao by lazy { database.equalizerDao() }
    private var musicId: Long = -1

    companion object {
        private const val ARG_MUSIC_ID = "music_id"

        fun newInstance(musicId: Long): EqualizerDialogFragment {
            val fragment = EqualizerDialogFragment()
            val args = Bundle()
            args.putLong(ARG_MUSIC_ID, musicId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            musicId = it.getLong(ARG_MUSIC_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEqualizerBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings()
    }


    private fun loadSettings() {
        lifecycleScope.launch {
            val settings = equalizerDao.getSettingsForMusic(musicId)
            settings?.let {
                binding.band1Seekbar.progress = it.band1Level + 1500
                binding.band2Seekbar.progress = it.band2Level + 1500
                binding.band3Seekbar.progress = it.band3Level + 1500
                binding.band4Seekbar.progress = it.band4Level + 1500
                binding.band5Seekbar.progress = it.band5Level + 1500
            }
        }
    }

    private fun saveSettings() {
        val settings = EqualizerSettings(
            musicId = musicId,
            band1Level = binding.band1Seekbar.progress - 1500,
            band2Level = binding.band2Seekbar.progress - 1500,
            band3Level = binding.band3Seekbar.progress - 1500,
            band4Level = binding.band4Seekbar.progress - 1500,
            band5Level = binding.band5Seekbar.progress - 1500
        )
        lifecycleScope.launch {
            equalizerDao.saveSettings(settings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Equalizer")
            .setView(R.layout.dialog_equalizer)
            .setPositiveButton("Save") { _, _ -> saveSettings() }
            .setNegativeButton("Cancel", null)
            .create()
}
