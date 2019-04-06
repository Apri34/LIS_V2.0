package com.lis.lis.database.entities

import androidx.room.*

@Entity(foreignKeys = [ForeignKey(entity = Stack::class,
    parentColumns = ["stack_id"],
    childColumns = ["stack_id"],
    onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["stack_id"])])
data class Card (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "card_id") val cardId: Int = 0,
    @ColumnInfo(name = "stack_id") var stackId: Int,
    @ColumnInfo(name = "val_1") var val1: String,
    @ColumnInfo(name = "val_2") var val2: String
)
