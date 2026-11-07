package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_calculations")
data class SavedCalculation(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val type: String, // "DILUTION", "HYDROMETER", "BLEND", "STILL_RUN"
  val timestamp: Long = System.currentTimeMillis(),
  val inputSummary: String,
  val resultSummary: String,
  val secondarySummary: String = "",
  val notes: String = ""
)
