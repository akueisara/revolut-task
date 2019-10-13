package com.example.revoluttask

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.revoluttask.ui.currencyrates.CurrencyRatesViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val app: Application) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            if (isAssignableFrom(CurrencyRatesViewModel::class.java)) {
                CurrencyRatesViewModel(app)
            } else {
                throw IllegalArgumentException("Unable to construct ${modelClass.name}")
            }
        } as T
}
