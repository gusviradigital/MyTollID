package com.gdp.mytollid.ui.manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gdp.mytollid.databinding.FragmentManualInputBinding

class ManualInputFragment : Fragment() {
    private var _binding: FragmentManualInputBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        binding.buttonCheck.setOnClickListener {
            val cardNumber = binding.editCardNumber.text.toString()
            if (cardNumber.isNotEmpty()) {
                checkCard(cardNumber)
            } else {
                binding.editCardNumber.error = "Nomor kartu tidak boleh kosong"
            }
        }
    }

    private fun checkCard(cardNumber: String) {
        // TODO: Implement card checking
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 