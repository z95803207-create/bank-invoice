package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.LedgerDao
import com.example.data.local.dao.VoucherDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.local.entity.VoucherEntity
import com.example.data.local.entity.VoucherLineEntity

@Database(
    entities = [
        AccountEntity::class,
        VoucherEntity::class,
        VoucherLineEntity::class,
        LedgerTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun voucherDao(): VoucherDao
    abstract fun ledgerDao(): LedgerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "accounting_app.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
