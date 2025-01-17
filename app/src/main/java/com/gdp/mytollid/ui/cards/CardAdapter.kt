package com.gdp.mytollid.ui.cards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gdp.mytollid.data.entity.Card
import com.gdp.mytollid.databinding.ItemCardBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class CardAdapter(
    private val onCardClick: (Card) -> Unit,
    private val onEditClick: (Card) -> Unit
) : ListAdapter<Card, CardAdapter.ViewHolder>(CardDiffCallback()) {

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = getItem(position)
        holder.bind(card)
    }

    inner class ViewHolder(
        private val binding: ItemCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCardClick(getItem(position))
                }
            }

            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(getItem(position))
                }
                true
            }
        }

        fun bind(card: Card) {
            binding.apply {
                textCardAlias.text = if (card.cardAlias.isNotEmpty()) card.cardAlias else card.cardName
                textCardNumber.text = formatCardNumber(card.cardNumber)
                textCardType.text = card.cardName
                textBalance.text = numberFormat.format(card.balance)
                textLastCheck.text = "Terakhir cek: ${dateFormat.format(card.lastCheck)}"
                chipCategory.text = card.category.name
                
                if (card.notes.isNotEmpty()) {
                    textNotes.visibility = View.VISIBLE
                    textNotes.text = card.notes
                } else {
                    textNotes.visibility = View.GONE
                }
            }
        }

        private fun formatCardNumber(cardNumber: String): String {
            return cardNumber.chunked(4).joinToString(" ")
        }
    }
}

private class CardDiffCallback : DiffUtil.ItemCallback<Card>() {
    override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
        return oldItem.cardNumber == newItem.cardNumber
    }

    override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
        return oldItem == newItem
    }
} 