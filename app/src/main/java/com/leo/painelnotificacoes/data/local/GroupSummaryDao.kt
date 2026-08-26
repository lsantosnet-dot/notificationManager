package com.leo.painelnotificacoes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupSummaryDao {

    @Query("SELECT * FROM group_summaries WHERE packageName = :packageName")
    fun observeSummary(packageName: String): Flow<GroupSummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: GroupSummaryEntity)

    @Query("DELETE FROM group_summaries WHERE packageName = :packageName")
    suspend fun deleteForPackage(packageName: String)
}
