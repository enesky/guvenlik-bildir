package com.enesky.guvenlikbildir.database.dao

import androidx.room.*
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.entity.SmsReport
import kotlinx.coroutines.flow.Flow

/**
 * Created by Enes Kamil YILMAZ on 19.05.2020
 */

@Dao
interface SmsReportDao {

    @Query("SELECT * FROM smsReport")
    suspend fun getAllReports(): List<SmsReport>

    @Query("SELECT * FROM smsReport")
    fun getAllReportsAsFlow(): Flow<List<SmsReport>>

    @Query("SELECT * FROM smsReport WHERE isSafeSms == 1")
    fun getSafeSmsListAsFlow(): Flow<List<SmsReport>>

    @Query("SELECT * FROM smsReport WHERE isSafeSms == 0")
    fun getUnsafeSmsListAsFlow(): Flow<List<SmsReport>>

    @Query("DELETE FROM smsReport")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg smsReport: SmsReport)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(smsReports: List<SmsReport>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(smsReports: SmsReport): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAll(smsReports: List<SmsReport>): Int

    @Delete
    suspend fun delete(smsReports: SmsReport)

}