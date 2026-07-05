package com.budgetpilot.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.budgetpilot.core.database.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExpenseDao {
    @Query(
        """
        SELECT * FROM expenses
        WHERE (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:merchant IS NULL OR merchant LIKE '%' || :merchant || '%')
          AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, id DESC
        """,
    )
    fun observeExpenses(
        categoryId: Long?,
        merchant: String?,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query(
        """
        SELECT categoryId, SUM(amountCentavos) AS totalCentavos
        FROM expenses
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY categoryId
        """,
    )
    suspend fun sumByCategory(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<CategorySpend>

    @Query(
        """
        SELECT strftime('%Y-%m', date) AS month, SUM(amountCentavos) AS totalCentavos
        FROM expenses
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY month
        ORDER BY month ASC
        """,
    )
    suspend fun sumByMonth(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<MonthSpend>
}
