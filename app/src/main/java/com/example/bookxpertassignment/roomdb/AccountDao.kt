package com.example.bookxpertassignment.roomdb

import androidx.room.*
import com.example.bookxpertassignment.model.Account

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<Account>)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): List<Account>

    @Query("SELECT * FROM accounts WHERE actid = :id")
    suspend fun getAccountById(id: Int): Account?
}
