package com.example.revoluttask.ui.currencyrates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.revoluttask.R
import com.example.revoluttask.model.Rate
import com.example.revoluttask.network.RevolutApi
import com.example.revoluttask.network.RevolutApiStatus
import com.example.revoluttask.network.latestrate.Rates
import kotlinx.coroutines.*
import kotlin.reflect.full.memberProperties

class CurrencyRatesViewModel(val app: Application): AndroidViewModel(app) {

    private companion object {
        const val DEFAULT_CURRENCY_CODE = "EUR"
        const val DEFAULT_CURRENCY_RATE = 1.0
    }

    private var currencyCode: String = DEFAULT_CURRENCY_CODE

    private var currencyRate: Double = DEFAULT_CURRENCY_RATE

    private val _status = MutableLiveData<RevolutApiStatus>()

    val status: LiveData<RevolutApiStatus>
        get() = _status

    private val _ratesList = MutableLiveData<MutableList<Rate>>()

    val rateList: LiveData<MutableList<Rate>>
        get() = _ratesList

    private var viewModelJob: Job? = null

    fun pauseJob() {
        viewModelJob?.cancel()
    }

    fun resumeJob() {
        getLatestRates()
    }

    private fun getLatestRates() {
        viewModelJob?.cancel()
        viewModelJob = viewModelScope.launch {
            _status.value = RevolutApiStatus.LOADING
            while(true) {
                val getLatestRatesDeferred = RevolutApi.retrofitService.getLatestRates(currencyCode).await()
                try {
                    val result = getLatestRatesDeferred.rates
                    _status.value = RevolutApiStatus.DONE
                    _ratesList.value = ratesToRateList(result, currencyCode)
                } catch (e: Exception) {
                    _status.value = RevolutApiStatus.ERROR
                    _ratesList.value = ArrayList()
                    break
                }
                delay(1_000)
            }
        }
    }

    private fun ratesToRateList(rates: Rates, baseRate: String): ArrayList<Rate> {
        val rateList: ArrayList<Rate> = ArrayList()
        rateList.add(Rate(baseRate, currencyRate, getFlagImageResId(baseRate)))
        Rates::class.memberProperties.forEach { member ->
            val rate = member.get(rates) as Double?
            if (rate != null) {
                rateList.add(Rate(member.name, rate * currencyRate, getFlagImageResId(member.name)))
            }
        }
        return rateList
    }

    private fun getFlagImageResId(code: String): Int {
        when (code) {
            "USD" -> return R.drawable.ic_flag_usd
            "AUD" -> return R.drawable.ic_flag_aud
            "BGN" -> return R.drawable.ic_flag_bgn
            "BRL" -> return R.drawable.ic_flag_brl
            "CAD" -> return R.drawable.ic_flag_cad
            "CHF" -> return R.drawable.ic_flag_chf
            "CNY" -> return R.drawable.ic_flag_cny
            "CZK" -> return R.drawable.ic_flag_czk
            "DKK" -> return R.drawable.ic_flag_dkk
            "EUR" -> return R.drawable.ic_flag_eur
            "GBP" -> return R.drawable.ic_flag_gbp
            "HKD" -> return R.drawable.ic_flag_hkd
            "HRK" -> return R.drawable.ic_flag_hrk
            "HUF" -> return R.drawable.ic_flag_huf
            "IDR" -> return R.drawable.ic_flag_idr
            "ILS" -> return R.drawable.ic_flag_ils
            "INR" -> return R.drawable.ic_flag_inr
            "ISK" -> return R.drawable.ic_flag_isk
            "JPY" -> return R.drawable.ic_flag_jpy
            "KRW" -> return R.drawable.ic_flag_krw
            "MXN" -> return R.drawable.ic_flag_mxn
            "MYR" -> return R.drawable.ic_flag_myr
            "NOK" -> return R.drawable.ic_flag_nok
            "NZD" -> return R.drawable.ic_flag_nzd
            "PHP" -> return R.drawable.ic_flag_php
            "PLN" -> return R.drawable.ic_flag_pln
            "RON" -> return R.drawable.ic_flag_ron
            "RUB" -> return R.drawable.ic_flag_rub
            "SEK" -> return R.drawable.ic_flag_sek
            "SGD" -> return R.drawable.ic_flag_sgd
            "THB" -> return R.drawable.ic_flag_thb
            "TRY" -> return R.drawable.ic_flag_try
            "ZAR" -> return R.drawable.ic_flag_zar
            else -> return R.drawable.ic_flag_placeholder

        }
    }

    fun onUpdateRates(rateList: MutableList<Rate>, moveToTop: Boolean) {
        if (rateList.size > 0) {
            currencyCode = rateList[0].code
            currencyRate = rateList[0].rate
            if(moveToTop) {
                getLatestRates()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob?.cancel()
    }
}