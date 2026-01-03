package com.example.miniexpensetracker
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miniexpensetracker.ui.theme.MiniExpenseTrackerTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.miniexpensetracker.data.ExpenseDatabase
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MiniExpenseTrackerTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val context = LocalContext.current
    val database = ExpenseDatabase.getDatabase(context)
    val dao = database.expenseDao()

    val expenses by dao.getAllExpenses().collectAsState(initial = emptyList())

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                ExpenseListScreen(
                    expenses = expenses,
                    onAddClick = {
                        navController.navigate(Screen.AddExpense.route)
                    },
                    onDeleteClick = { expense ->
                        scope.launch {
                            dao.deleteExpense(expense)
                        }
                    },
                    onExpenseClick = { expense ->
                        navController.navigate(
                            Screen.EditExpense.route + "/${expense.id}"
                        )
                    }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionListScreen(
                    expenses = expenses,
                    onExpenseClick = { expense ->
                        navController.navigate(
                            Screen.EditExpense.route + "/${expense.id}"
                        )
                    }
                )
            }

            composable(Screen.AddExpense.route) {
                AddExpenseScreen(
                    onSave = { expense ->
                        scope.launch {
                            dao.insertExpense(expense)
                        }
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.EditExpense.route + "/{id}"
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
                val expenseToEdit = expenses.find { it.id == id }

                AddExpenseScreen(
                    existingExpense = expenseToEdit,
                    onSave = { updatedExpense ->
                        scope.launch {
                            dao.updateExpense(updatedExpense)
                        }
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun TransactionListScreen(
    expenses: List<Expense>,
    onExpenseClick: (Expense) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "All Transactions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(expenses) { expense ->
                ExpenseItem(
                    expense = expense,
                    onClick = { onExpenseClick(expense) },
                    onDelete = { /* Logic for delete if needed */ }
                )
            }
        }
    }
}

@Composable
fun ExpenseListScreen(
    expenses: List<Expense>,
    onAddClick: () -> Unit,
    onDeleteClick: (Expense) -> Unit,
    onExpenseClick: (Expense) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Groceries", "Travel", "Food", "Other")

    val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
    val totalLast7Days = expenses
        .filter { it.date >= sevenDaysAgo }
        .sumOf { it.amount }

    val filteredExpenses = expenses.filter { expense ->
        val matchesSearch = expense.title.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || expense.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mini Expense Tracker",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add Expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Last 7 days",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "₹ $totalLast7Days",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search expenses") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        CategoryDropdown(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(filteredExpenses) { expense ->
                ExpenseItem(
                    expense = expense,
                    onClick = { onExpenseClick(expense) },
                    onDelete = { onDeleteClick(expense) }
                )
            }
        }
    }
}

@Composable
fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Category: $selectedCategory")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                Text(
                    text = sdf.format(Date(expense.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹ ${expense.amount}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDelete) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddExpenseScreen(
    existingExpense: Expense? = null,
    onSave: (Expense) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(existingExpense?.title ?: "") }
    var amount by remember { mutableStateOf(existingExpense?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(existingExpense?.category ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (existingExpense == null) "Add Expense" else "Edit Expense",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = title,
            onValueChange = {
                title = it
                titleError = null
            },
            label = { Text("Title") },
            isError = titleError != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (titleError != null) {
            Text(text = titleError!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = amount,
            onValueChange = {
                amount = it
                amountError = null
            },
            label = { Text("Amount") },
            isError = amountError != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (amountError != null) {
            Text(text = amountError!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                var isValid = true
                if (title.isBlank()) {
                    titleError = "Title cannot be empty"
                    isValid = false
                }
                val amountValue = amount.toDoubleOrNull()
                if (amountValue == null || amountValue <= 0) {
                    amountError = "Amount must be greater than 0"
                    isValid = false
                }

                if (!isValid) return@Button

                val expense = Expense(
                    id = existingExpense?.id ?: 0,
                    title = title,
                    amount = amountValue!!,
                    category = category,
                    date = existingExpense?.date ?: System.currentTimeMillis()
                )
                onSave(expense)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}