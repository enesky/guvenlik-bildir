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
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contact WHERE id == :id")
    fun getContact(id: Int): Flow<Contact>

    @Query("DELETE FROM contact")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(earthquakes: List<Contact>)

    @Delete
    fun delete(contact: Contact)

}