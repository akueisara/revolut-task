package com.example.revoluttask.util

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
        val nf = NumberFormat.getInstance(Locale.US)
        val df = nf.parse(value).toDouble()
        return df
    }
}