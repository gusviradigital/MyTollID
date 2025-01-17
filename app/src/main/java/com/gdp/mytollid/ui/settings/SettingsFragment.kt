package com.gdp.mytollid.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gdp.mytollid.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel

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
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationEnabled(isChecked)
        }

        binding.buttonPremium.setOnClickListener {
            showPremiumDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.isPremium.observe(viewLifecycleOwner) { isPremium ->
            updatePremiumUI(isPremium)
        }

        viewModel.notificationEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.switchNotification.isChecked = isEnabled
        }

        viewModel.purchaseEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                SettingsViewModel.PurchaseEvent.LOADING -> {
                    binding.buttonPremium.isEnabled = false
                    binding.buttonPremium.text = "Memproses..."
                }
                SettingsViewModel.PurchaseEvent.SUCCESS -> {
                    binding.buttonPremium.isEnabled = true
                    Toast.makeText(context, "Berhasil upgrade ke Premium!", Toast.LENGTH_LONG).show()
                }
                SettingsViewModel.PurchaseEvent.CANCELED -> {
                    binding.buttonPremium.isEnabled = true
                    binding.buttonPremium.text = "Upgrade ke Premium"
                    Toast.makeText(context, "Pembelian dibatalkan", Toast.LENGTH_SHORT).show()
                }
                SettingsViewModel.PurchaseEvent.ERROR -> {
                    binding.buttonPremium.isEnabled = true
                    binding.buttonPremium.text = "Upgrade ke Premium"
                    Toast.makeText(context, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updatePremiumUI(isPremium: Boolean) {
        if (isPremium) {
            binding.buttonPremium.text = "Premium Aktif"
            binding.buttonPremium.isEnabled = false
        } else {
            binding.buttonPremium.text = "Upgrade ke Premium"
            binding.buttonPremium.isEnabled = true
        }
    }

    private fun showPremiumDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Upgrade ke Premium")
            .setMessage("Nikmati fitur premium tanpa iklan dan backup data dengan sekali bayar.")
            .setPositiveButton("Lanjutkan") { _, _ ->
                viewModel.initiatePurchase()
                viewModel.getBillingManager().launchBillingFlow(requireActivity())
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 