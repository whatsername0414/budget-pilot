package com.budgetpilot.core.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.budgetpilot.core.database.dao.BudgetDao
import com.budgetpilot.core.database.dao.CategoryDao
import com.budgetpilot.core.database.dao.ExpenseDao
import com.budgetpilot.core.database.dao.ExtractionCacheDao
import com.budgetpilot.core.database.dao.InsightDao
import com.budgetpilot.core.database.entity.BudgetEntity
import com.budgetpilot.core.database.entity.CategoryEntity
import com.budgetpilot.core.database.entity.ExpenseEntity
import com.budgetpilot.core.database.entity.ExtractionCacheEntity
import com.budgetpilot.core.database.entity.InsightEntity
import com.budgetpilot.core.database.seed.DefaultCategories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val DATABASE_NAME = "budgetpilot.db"

@Database(
    entities = [
        CategoryEntity::class,
        ExpenseEntity::class,
        BudgetEntity::class,
        ExtractionCacheEntity::class,
        InsightEntity::class,
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ],
)
@TypeConverters(Converters::class)
abstract class BudgetPilotDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    abstract fun expenseDao(): ExpenseDao

    abstract fun budgetDao(): BudgetDao

    abstract fun extractionCacheDao(): ExtractionCacheDao

    abstract fun insightDao(): InsightDao
}

fun buildBudgetPilotDatabase(context: Context): BudgetPilotDatabase {
    lateinit var database: BudgetPilotDatabase
    database =
        Room
            .databaseBuilder(context, BudgetPilotDatabase::class.java, DATABASE_NAME)
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            database.categoryDao().insertAll(DefaultCategories.all)
                        }
                    }
                },
            ).build()
    return database
}
