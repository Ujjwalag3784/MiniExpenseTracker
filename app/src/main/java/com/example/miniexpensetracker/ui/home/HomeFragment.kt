package com.example.miniexpensetracker.ui.home
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
import com.example.miniexpensetracker.ExpenseListScreen
import com.example.miniexpensetracker.viewmodel.ExpenseViewModel
import com.example.miniexpensetracker.viewmodel.ExpenseViewModelFactory
import com.example.miniexpensetracker.data.ExpenseDatabase
import com.example.miniexpensetracker.R

class HomeFragment : Fragment() {

    private val viewModel: ExpenseViewModel by viewModels {
        val database = ExpenseDatabase.getDatabase(requireContext())
        ExpenseViewModelFactory(database.expenseDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val expenses by viewModel.expenses.collectAsState()

                ExpenseListScreen(
                    expenses = expenses,
                    onAddClick = {
                        findNavController().navigate(R.id.action_homeFragment_to_addExpenseFragment)
                    },
                    onDeleteClick = { expense ->
                        viewModel.deleteExpense(expense)
                    },
                    onExpenseClick = { expense ->
                        val bundle = Bundle().apply {
                            putInt("expenseId", expense.id)
                        }
                        findNavController().navigate(R.id.action_homeFragment_to_editExpenseFragment, bundle)
                    }
                )
            }
        }
    }
}
