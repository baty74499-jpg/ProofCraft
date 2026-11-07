package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCalculationDao {
  @Query("SELECT * FROM saved_calculations ORDER BY timestamp DESC")
  fun getAllCalculations(): Flow<List<SavedCalculation>>

  @Query("SELECT * FROM saved_calculations WHERE type = :type ORDER BY timestamp DESC")
  fun getCalculationsByType(type: String): Flow<List<SavedCalculation>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCalculation(calculation: SavedCalculation): Long

  @Delete
  suspend fun deleteCalculation(calculation: SavedCalculation)

  @Query("DELETE FROM saved_calculations WHERE id = :id")
  suspend fun deleteById(id: Long)
}
