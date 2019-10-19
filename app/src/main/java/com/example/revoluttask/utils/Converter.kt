package com.example.revoluttask.utils

import androidx.databinding.InverseMethod
import java.text.NumberFormat
import java.util.*

object Converter {
    @InverseMethod("rateStringToDouble")
    @JvmStatic
    fun doubleToRateString(value: Double): String {
        return String.format("%,.2f", value)
    }

    @JvmStatic
    fun rateStringToDouble(value: String): Double {
        if(value.isEmpty() || value.toDoubleOrNull() == 0.0) {
            return 1.0
        }
        val nf = NumberFormat.getInstance(Locale.US)
        return nf.parse(value).toDouble()
    }

    @JvmStatic
    fun currencyCodeToName(code: String): String {
        return Currency.getInstance(code).displayName
    }
}