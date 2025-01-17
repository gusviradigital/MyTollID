package com.gdp.mytollid.ui.cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdp.mytollid.data.entity.Card
import com.gdp.mytollid.data.entity.CardCategory
import com.gdp.mytollid.databinding.DialogEditCardBinding
import com.gdp.mytollid.databinding.FragmentCardsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CardsFragment : Fragment() {
    private var _binding: FragmentCardsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CardsViewModel
    private lateinit var adapter: CardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CardsViewModel::class.java]
        setupRecyclerView()
        setupCategoryFilter()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = CardAdapter(
            onCardClick = { card ->
                // TODO: Navigate to card detail
            },
            onEditClick = { card ->
                showEditDialog(card)
            }
        )

        binding.recyclerCards.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CardsFragment.adapter
        }
    }

    private fun setupCategoryFilter() {
        val categories = CardCategory.values().map { it.name }
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )

        (binding.spinnerCategory as? AutoCompleteTextView)?.apply {
            setAdapter(categoryAdapter)
            setOnItemClickListener { _, _, position, _ ->
                val category = CardCategory.values()[position]
                viewModel.setCategory(category)
            }
        }

        binding.chipAllCards.setOnClickListener {
            viewModel.setCategory(null)
            binding.spinnerCategory.setText("")
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.setSearchQuery(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.setSearchQuery(it) }
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.cards.observe(viewLifecycleOwner) { cards ->
            adapter.submitList(cards)
            binding.textEmpty.visibility = if (cards.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showEditDialog(card: Card) {
        val dialogBinding = DialogEditCardBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.apply {
            editCardNumber.setText(card.cardNumber)
            editCardAlias.setText(card.cardAlias)
            editNotes.setText(card.notes)

            val categories = CardCategory.values()
            val categoryAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories.map { it.name }
            )
            spinnerCategory.setAdapter(categoryAdapter)
            spinnerCategory.setText(card.category.name, false)

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnSave.setOnClickListener {
                val category = CardCategory.valueOf(spinnerCategory.text.toString())
                viewModel.updateCard(
                    cardNumber = card.cardNumber,
                    alias = editCardAlias.text.toString(),
                    category = category,
                    notes = editNotes.text.toString()
                )
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 