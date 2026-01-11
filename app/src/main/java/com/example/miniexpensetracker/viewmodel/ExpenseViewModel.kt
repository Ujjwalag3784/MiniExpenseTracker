package com.example.miniexpensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniexpensetracker.Expense
import com.example.miniexpensetracker.data.ExpenseDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val dao: ExpenseDao
) : ViewModel() {

    // ✅ Single source of truth for expenses
    val expenses: StateFlow<List<Expense>> =
        dao.getAllExpenses()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            dao.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            dao.deleteExpense(expense)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            dao.updateExpense(expense)
        }
    }
}
