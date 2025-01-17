package com.gdp.mytollid.ui.manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gdp.mytollid.databinding.FragmentManualInputBinding
import com.gdp.mytollid.util.card.CardType

class ManualInputFragment : Fragment() {
    private var _binding: FragmentManualInputBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ManualInputViewModel

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
        viewModel = ViewModelProvider(this)[ManualInputViewModel::class.java]
        setupViews()
        observeViewModel()
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

    private fun observeViewModel() {
        viewModel.cardState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CardState.Success -> {
                    binding.textResult.visibility = View.VISIBLE
                    binding.textResult.text = """
                        Nomor Kartu: ${formatCardNumber(state.card.cardNumber)}
                        Saldo: Rp ${state.card.balance}
                        Tipe: ${state.card.cardName}
                        ${if (state.card.cardAlias.isNotEmpty()) "Alias: ${state.card.cardAlias}" else ""}
                    """.trimIndent()
                }
                is CardState.Error -> {
                    binding.textResult.visibility = View.VISIBLE
                    binding.textResult.text = state.message
                }
            }
        }
    }

    private fun checkCard(cardNumber: String) {
        if (cardNumber.length != 16) {
            binding.editCardNumber.error = "Nomor kartu harus 16 digit"
            return
        }

        binding.buttonCheck.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        
        viewModel.checkCard(cardNumber)
    }

    private fun formatCardNumber(cardNumber: String): String {
        return cardNumber.chunked(4).joinToString(" ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 