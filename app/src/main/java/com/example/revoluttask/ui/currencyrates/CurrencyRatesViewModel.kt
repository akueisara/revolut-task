package com.example.revoluttask.ui.currencyrates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.revoluttask.model.Rate
import com.example.revoluttask.network.RevolutApi
import com.example.revoluttask.network.RevolutApiStatus
import com.example.revoluttask.network.latestrate.Rates
import kotlinx.coroutines.*
import timber.log.Timber
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
        rateList.add(Rate(baseRate, DEFAULT_CURRENCY_RATE * currencyRate))
        Rates::class.memberProperties.forEach { member ->
            val rate = member.get(rates) as Double?
            if (rate != null) {
                rateList.add(Rate(member.name, rate * currencyRate))
            }
        }
        return rateList
    }

    fun onUpdateRate(rate: Rate) {
        Timber.d("onUpdateRate")
        _ratesList.value!![0] = rate
        currencyCode = rate.code
        currencyRate = rate.rate
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob?.cancel()
    }
}