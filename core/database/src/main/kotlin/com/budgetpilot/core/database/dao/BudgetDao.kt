package com.budgetpilot.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.budgetpilot.core.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month")
    fun observeBudgetsForMonth(month: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month")
    suspend fun getBudget(
        categoryId: Long,
        month: String,
    ): BudgetEntity?

    @Insert
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query(
        """
        SELECT COALESCE(SUM(amountCentavos), 0) FROM expenses
        WHERE categoryId = :categoryId AND strftime('%Y-%m', date) = :month
        """,
    )
    suspend fun spentForCategoryInMonth(
        categoryId: Long,
        month: String,
    ): Long
}
