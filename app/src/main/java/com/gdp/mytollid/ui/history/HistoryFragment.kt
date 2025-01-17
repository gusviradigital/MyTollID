package com.gdp.mytollid.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdp.mytollid.data.entity.Transaction
import com.gdp.mytollid.data.entity.TransactionType
import com.gdp.mytollid.databinding.DialogTransactionDetailBinding
import com.gdp.mytollid.databinding.FragmentHistoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: TransactionAdapter
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        setupRecyclerView()
        setupCardSpinner()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter { transaction ->
            showTransactionDetail(transaction)
        }
        binding.recyclerHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HistoryFragment.adapter
        }
    }

    private fun setupCardSpinner() {
        viewModel.activeCards.observe(viewLifecycleOwner) { cards ->
            val cardNumbers = mutableListOf("Semua Kartu")
            cardNumbers.addAll(cards.map { formatCardNumber(it.cardNumber) })
            
            val spinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                cardNumbers
            )
            
            (binding.spinnerCard as? AutoCompleteTextView)?.apply {
                setAdapter(spinnerAdapter)
                setText(cardNumbers[0], false)
                setOnItemClickListener { _, _, position, _ ->
                    val selectedCardNumber = if (position == 0) "" else cards[position - 1].cardNumber
                    viewModel.setSelectedCard(selectedCardNumber)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
        }
    }

    private fun showTransactionDetail(transaction: Transaction) {
        val dialogBinding = DialogTransactionDetailBinding.inflate(layoutInflater)
        dialogBinding.apply {
            textTransactionType.text = when (transaction.type) {
                TransactionType.CHECK_BALANCE -> "Cek Saldo"
                TransactionType.TOP_UP -> "Top Up"
                TransactionType.PAYMENT -> "Pembayaran"
            }
            textCardNumber.text = formatCardNumber(transaction.cardNumber)
            textDate.text = dateFormat.format(transaction.date)
            textAmount.text = when (transaction.type) {
                TransactionType.CHECK_BALANCE -> "-"
                TransactionType.TOP_UP -> "+${numberFormat.format(transaction.amount)}"
                TransactionType.PAYMENT -> "-${numberFormat.format(transaction.amount)}"
            }
            textBalance.text = numberFormat.format(transaction.balance)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .show()
            .apply {
                dialogBinding.buttonClose.setOnClickListener {
                    dismiss()
                }
            }
    }

    private fun formatCardNumber(cardNumber: String): String {
        return if (cardNumber.length >= 16) {
            val masked = cardNumber.substring(0, 12).map { '*' }.joinToString("")
            masked + cardNumber.substring(12)
        } else {
            cardNumber
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 