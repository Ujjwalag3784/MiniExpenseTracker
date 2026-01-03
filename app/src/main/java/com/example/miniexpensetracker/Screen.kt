package com.example.miniexpensetracker
sealed class Screen(val route: String) {

    // Bottom navigation screens
    object Home : Screen("home")
    object Transactions : Screen("transactions")

    // Other screens
    object AddExpense : Screen("add_expense")
    object EditExpense : Screen("edit_expense")
}
