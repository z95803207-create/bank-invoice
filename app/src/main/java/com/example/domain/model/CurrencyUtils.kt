package com.example.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    private val symbols = DecimalFormatSymbols(Locale.US)
    private val standardFormat = DecimalFormat("#,##0.00", symbols)
    private val currencySymbolFormat = DecimalFormat("$#,##0.00", symbols)

    fun format(cents: Long, includeSymbol: Boolean = false): String {
        val amount = cents.toDouble() / 100.0
        return if (includeSymbol) currencySymbolFormat.format(amount) else standardFormat.format(amount)
    }

    fun parseToCents(input: String): Long {
        val clean = input.trim().replace("$", "").replace(",", "").trim()
        if (clean.isEmpty()) return 0L
        return try {
            val bd = BigDecimal(clean).setScale(2, RoundingMode.HALF_UP)
            (bd.multiply(BigDecimal(100))).toLong()
        } catch (e: Exception) {
            0L
        }
    }

    fun doubleToCents(amount: Double): Long {
        return try {
            BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100)).toLong()
        } catch (e: Exception) {
            0L
        }
    }

    fun centsToDouble(cents: Long): Double {
        return cents.toDouble() / 100.0
    }
}
