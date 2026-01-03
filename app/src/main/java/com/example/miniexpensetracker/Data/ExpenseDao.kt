package com.example.miniexpensetracker.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.example.miniexpensetracker.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {


    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

}
