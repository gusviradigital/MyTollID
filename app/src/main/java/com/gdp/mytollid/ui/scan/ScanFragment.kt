package com.gdp.mytollid.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.gdp.mytollid.databinding.FragmentScanBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanFragment : Fragment() {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ScanViewModel
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ScanViewModel::class.java]
        setupViews()
        observeViewModel()

        // Check for arguments from NFC scan
        arguments?.let {
            val cardNumber = it.getString("cardNumber")
            val balance = it.getDouble("balance")
            if (cardNumber != null) {
                onCardScanned(cardNumber, balance)
            }
        }
    }

    private fun setupViews() {
        binding.textBalance.text = numberFormat.format(0)
        binding.textLastCheck.text = "Terakhir cek: -"
    }

    private fun observeViewModel() {
        viewModel.scanResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ScanResult.Success -> {
                    binding.textBalance.text = numberFormat.format(result.card.balance)
                    binding.textLastCheck.text = "Terakhir cek: ${dateFormat.format(result.card.lastCheck)}"
                }
                is ScanResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onCardScanned(cardNumber: String, balance: Double) {
        viewModel.processNfcCard(cardNumber, balance)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 