package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY code ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts ORDER BY code ASC")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE code = :code LIMIT 1")
    suspend fun getAccountByCode(code: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE parentId = :parentId ORDER BY code ASC")
    suspend fun getAccountsByParentId(parentId: Long): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE level = 4 AND isActive = 1 ORDER BY code ASC")
    fun getPostingAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE level = 4 AND isActive = 1 ORDER BY code ASC")
    suspend fun getPostingAccounts(): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountsCount(): Int

    @Query("SELECT COUNT(*) FROM accounts WHERE level = :level")
    suspend fun countByLevel(level: Int): Int
}
