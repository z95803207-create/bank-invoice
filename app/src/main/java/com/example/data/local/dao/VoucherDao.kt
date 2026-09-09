package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.VoucherEntity
import com.example.data.local.entity.VoucherLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {
    @Query("SELECT * FROM vouchers ORDER BY voucherDate DESC, id DESC")
    fun getAllVouchersFlow(): Flow<List<VoucherEntity>>

    @Query("SELECT * FROM vouchers ORDER BY voucherDate DESC, id DESC")
    suspend fun getAllVouchers(): List<VoucherEntity>

    @Query("SELECT * FROM vouchers WHERE id = :id LIMIT 1")
    suspend fun getVoucherById(id: Long): VoucherEntity?

    @Query("SELECT * FROM vouchers WHERE voucherNumber = :voucherNumber LIMIT 1")
    suspend fun getVoucherByNumber(voucherNumber: String): VoucherEntity?

    @Query("SELECT * FROM voucher_lines WHERE voucherId = :voucherId ORDER BY id ASC")
    suspend fun getLinesForVoucher(voucherId: Long): List<VoucherLineEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVoucher(voucher: VoucherEntity): Long

    @Update
    suspend fun updateVoucher(voucher: VoucherEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVoucherLines(lines: List<VoucherLineEntity>)

    @Query("DELETE FROM voucher_lines WHERE voucherId = :voucherId")
    suspend fun deleteLinesForVoucher(voucherId: Long)

    @Query("SELECT COUNT(*) FROM vouchers")
    suspend fun getVouchersCount(): Int

    @Query("SELECT COUNT(*) FROM vouchers WHERE isPosted = 0")
    suspend fun getDraftCount(): Int

    @Query("SELECT COUNT(*) FROM vouchers WHERE isPosted = 1")
    suspend fun getPostedCount(): Int

    @Query("SELECT voucherNumber FROM vouchers WHERE voucherType = 'CP' ORDER BY id DESC LIMIT 1")
    suspend fun getLastCpVoucherNumber(): String?
}
