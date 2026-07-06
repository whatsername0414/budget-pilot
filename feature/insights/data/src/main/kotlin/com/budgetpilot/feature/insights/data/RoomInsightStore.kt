package com.budgetpilot.feature.insights.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.budgetpilot.core.database.dao.InsightDao
import com.budgetpilot.feature.insights.data.mapper.toEntity
import com.budgetpilot.feature.insights.domain.InsightHistoryStore
import com.budgetpilot.feature.insights.domain.InsightStore
import com.budgetpilot.feature.insights.domain.model.Insight
import com.budgetpilot.feature.insights.domain.model.InsightType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant

private val LAST_SHOWN_AT_MILLIS_KEY = longPreferencesKey("insight_last_shown_at_millis")

/**
 * Persists insights to Room (so [wasShown] can check the full type+month history) while keeping
 * the 48h cooldown timestamp in the shared preferences [DataStore] per PLAN.md §2's "insight
 * throttling timestamps" call-out — cheaper than a table scan and consistent with how
 * `:core:data`'s `UserPreferences` already uses this same DataStore file for small scalar state.
 */
class RoomInsightStore(
    private val dao: InsightDao,
    private val dataStore: DataStore<Preferences>,
) : InsightStore,
    InsightHistoryStore {
    override suspend fun save(insight: Insight): Long {
        val id = dao.insert(insight.toEntity())
        dataStore.edit { preferences -> preferences[LAST_SHOWN_AT_MILLIS_KEY] = insight.createdAt.toEpochMilli() }
        return id
    }

    override suspend fun lastShownAt(): Instant? =
        dataStore.data
            .map { preferences -> preferences[LAST_SHOWN_AT_MILLIS_KEY] }
            .first()
            ?.let(Instant::ofEpochMilli)

    override suspend fun wasShown(
        type: InsightType,
        month: String,
    ): Boolean = dao.wasShown(type.name, month)
}
