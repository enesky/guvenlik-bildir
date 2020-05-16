package com.enesky.guvenlikbildir.database.dao

import androidx.paging.DataSource
import androidx.room.*
import com.enesky.guvenlikbildir.database.entity.Contact
import kotlinx.coroutines.flow.Flow

/**
 * Created by Enes Kamil YILMAZ on 13.05.2020
 */

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact")
    fun getAllContactsDsF(): DataSource.Factory<Int, Contact>

    @Query("SELECT * FROM contact") // ORDER BY id ASC
    fun getAllContactsFlow(): Flow<List<Contact>>

    @Query("SELECT * FROM contact") // ORDER BY id ASC
    suspend fun getAllContacts(): List<Contact>

    @Query("SELECT * FROM contact WHERE isSelected == 1")
    fun getSelectedContactsFlow(): Flow<List<Contact>>

    @Query("SELECT * FROM contact WHERE isSelected == 0")
    fun getUnselectedContactsFlow(): Flow<List<Contact>>

    @Query("DELETE FROM contact")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<Contact>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(contact: Contact): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(contacts: List<Contact>): Int

    @Delete
    suspend fun delete(contact: Contact)

}