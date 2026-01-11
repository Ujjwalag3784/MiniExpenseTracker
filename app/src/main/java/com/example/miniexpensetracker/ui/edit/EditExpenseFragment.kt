package com.example.miniexpensetracker.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.miniexpensetracker.AddExpenseScreen
import com.example.miniexpensetracker.viewmodel.ExpenseViewModel
import com.example.miniexpensetracker.viewmodel.ExpenseViewModelFactory
import com.example.miniexpensetracker.data.ExpenseDatabase

class EditExpenseFragment : Fragment() {

    private val viewModel: ExpenseViewModel by viewModels {
        val database = ExpenseDatabase.getDatabase(requireContext())
        ExpenseViewModelFactory(database.expenseDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val expenseId = arguments?.getInt("expenseId") ?: 0

        return ComposeView(requireContext()).apply {
            setContent {
                val expenses by viewModel.expenses.collectAsState()
                val expenseToEdit = expenses.find { it.id == expenseId }

                AddExpenseScreen(
                    existingExpense = expenseToEdit,
                    onSave = { updatedExpense ->
                        viewModel.updateExpense(updatedExpense)
                        findNavController().popBackStack()
                    },
                    onBack = {
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }
}
