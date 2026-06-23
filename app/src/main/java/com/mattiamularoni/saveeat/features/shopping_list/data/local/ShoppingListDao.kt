package com.mattiamularoni.saveeat.features.shopping_list.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    @Query("SELECT * FROM shopping_list_items ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<ShoppingListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ShoppingListItemEntity): Long

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM shopping_list_items")
    suspend fun clear(): Int
}
