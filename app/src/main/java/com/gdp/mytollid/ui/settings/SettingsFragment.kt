package com.gdp.mytollid.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gdp.mytollid.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Save notification preference
        }

        binding.buttonPremium.setOnClickListener {
            // TODO: Handle premium upgrade
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 