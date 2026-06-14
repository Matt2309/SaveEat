package com.mattiamularoni.saveeat.features.pantry.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items WHERE user_id = :userId ORDER BY name ASC")
    fun getPantryItems(userId: String): Flow<List<PantryEntity>>

    @Query("SELECT * FROM pantry_items WHERE id = :itemId LIMIT 1")
    suspend fun getPantryItemById(itemId: String): PantryEntity?

    @Query("SELECT * FROM pantry_items WHERE user_id = :userId AND category = :category ORDER BY name ASC")
    fun getPantryItemsByCategory(userId: String, category: String): Flow<List<PantryEntity>>

    @Query("SELECT * FROM pantry_items WHERE user_id = :userId AND expiration_date <= :thresholdMs ORDER BY expiration_date ASC")
    fun getExpiringItems(userId: String, thresholdMs: Long): Flow<List<PantryEntity>>

    @Query("SELECT * FROM pantry_items WHERE user_id = :userId AND is_placeholder = 1 ORDER BY name ASC")
    suspend fun getPlaceholders(userId: String): List<PantryEntity>

    @Query("SELECT * FROM pantry_items WHERE user_id = :userId AND is_placeholder = 1 AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchPlaceholders(userId: String, query: String): List<PantryEntity>

    @Query("SELECT * FROM pantry_items WHERE user_id = :userId AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchPantryItems(userId: String, query: String): List<PantryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItem(item: PantryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItems(items: List<PantryEntity>)

    @Update
    suspend fun updatePantryItem(item: PantryEntity): Int

    @Delete
    suspend fun deletePantryItem(item: PantryEntity): Int

    @Query("DELETE FROM pantry_items WHERE id = :itemId")
    suspend fun deletePantryItemById(itemId: String): Int

    @Query("DELETE FROM pantry_items WHERE user_id = :userId AND is_placeholder = 1 AND id = :placeholderId")
    suspend fun deletePlaceholder(userId: String, placeholderId: String): Int

    @Query("SELECT COUNT(*) FROM pantry_items WHERE user_id = :userId AND name = :name AND category = :category AND is_placeholder = 0")
    suspend fun countDuplicates(userId: String, name: String, category: String): Int

    @Query("UPDATE pantry_items SET image_url = :imageUrl WHERE id = :itemId")
    suspend fun updateImageUrl(itemId: String, imageUrl: String)

    @Query("SELECT id, image_url FROM pantry_items WHERE user_id = :userId")
    suspend fun getImageUrlsForUser(userId: String): List<PantryImageUrlEntry>

    data class PantryImageUrlEntry(
        @ColumnInfo(name = "id") val id: String,
        @ColumnInfo(name = "image_url") val imageUrl: String?
    )
}
