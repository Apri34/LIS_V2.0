package com.lis.lis.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lis.lis.database.entities.Stack

@Dao
interface StackDao {
    @Query("SELECT stack_name FROM stack")
    fun getAll(): List<String>

    @Query("SELECT * FROM stack WHERE stack_name = :stackName")
    fun getByName(stackName: String): Stack

    @Insert
    fun insert(stack: Stack)

    @Query("DELETE FROM stack WHERE stack_name = :stackName")
    fun delete(stackName: String)
}