package com.gdp.mytollid.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gdp.mytollid.data.entity.Transaction
import com.gdp.mytollid.data.entity.TransactionType
import com.gdp.mytollid.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(TransactionDiffCallback()) {

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(transaction: Transaction) {
            binding.apply {
                textTransactionType.text = when (transaction.type) {
                    TransactionType.CHECK_BALANCE -> "Cek Saldo"
                    TransactionType.TOP_UP -> "Top Up"
                    TransactionType.PAYMENT -> "Pembayaran"
                }
                textCardNumber.text = "Kartu: ${formatCardNumber(transaction.cardNumber)}"
                textDate.text = dateFormat.format(transaction.date)
                textAmount.text = when (transaction.type) {
                    TransactionType.CHECK_BALANCE -> "-"
                    TransactionType.TOP_UP -> "+${numberFormat.format(transaction.amount)}"
                    TransactionType.PAYMENT -> "-${numberFormat.format(transaction.amount)}"
                }
                textBalance.text = "Saldo: ${numberFormat.format(transaction.balance)}"
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
    }
}

class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
} 