package com.lis.lis.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Stack(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "stack_id") val stackId: Int = 0,
    @ColumnInfo(name = "stack_name") val stackName: String,
    @ColumnInfo(name = "col_1") val column1: String,
    @ColumnInfo(name = "col_2") val column2: String
)