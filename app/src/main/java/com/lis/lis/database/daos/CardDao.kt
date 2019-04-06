package com.lis.lis.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lis.lis.database.entities.Card

@Dao
interface CardDao {
    @Query("SELECT * FROM card WHERE stack_id = (SELECT stack_id FROM stack WHERE stack_name = :stackName LIMIT 1)")
    fun getCardsByStackName(stackName: String): List<Card>

    @Insert
    fun insert(card: Card)

    @Query("DELETE FROM card WHERE card_id = :cardId")
    fun deleteCardById(cardId: Int)
}