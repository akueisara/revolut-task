package com.example.revoluttask.ui.currencyrates

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.revoluttask.R
import com.example.revoluttask.model.Rate
import com.example.revoluttask.model.RevolutDatabaseDao
import com.example.revoluttask.network.RevolutApi
import com.example.revoluttask.network.RevolutApiStatus
import com.example.revoluttask.network.latestrate.Rates
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import timber.log.Timber
import java.net.SocketTimeoutException
import kotlin.reflect.full.memberProperties

class CurrencyRatesViewModel(val database: RevolutDatabaseDao, val app: Application) : AndroidViewModel(app) {

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

    private lateinit var viewModelJob: Job

    private lateinit var coroutineScope: CoroutineScope

    private val _ratesListFromDB = MutableLiveData<MutableList<Rate>>()

    val ratesListFromDB: LiveData<MutableList<Rate>>
        get() = _ratesListFromDB

    private var latestRates: Rates? = null

    private var networkError = false

    fun pauseJob() {
        viewModelJob.cancel()
    }

    @ObsoleteCoroutinesApi
    fun resumeJob() {
        getLatestRates()
    }

    @ObsoleteCoroutinesApi
    private fun getLatestRates(showLoading: Boolean = true) {
        viewModelJob = Job()
        coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        coroutineScope.launch {
            if (showLoading) {
                _status.value = RevolutApiStatus.LOADING
            }
            val tickerChannel = ticker(delayMillis = 1_000, initialDelayMillis = 0)
            for (event in tickerChannel) {
                try {
                    val getLatestRatesDeferred = RevolutApi.retrofitService.getLatestRates(currencyCode).await()
                    _status.value = RevolutApiStatus.DONE
                    networkError = true
                    latestRates = getLatestRatesDeferred.rates
                    _ratesList.value = ratesToRateList(latestRates!!, currencyCode)
                    _ratesListFromDB.value = getRatesFromDatabase().toMutableList()
                } catch (e: Exception) {
                    Timber.e(e)
                    tickerChannel.cancel()
                    networkError = false
                    _ratesListFromDB.value = getRatesFromDatabase().toMutableList()
                }
            }
        }
    }

    fun updateRateList(rateListFromDB: MutableList<Rate>) {
        coroutineScope.launch {
            if (_status.value != RevolutApiStatus.DONE || !networkError) {
                if (rateListFromDB.size != 0) {
                    if(!networkError) {
                        _status.value = RevolutApiStatus.LOCALDATA
                    } else {
                        _status.value = RevolutApiStatus.DONE
                    }
                    if (latestRates != null) {
                        _ratesList.value = ratesToRateList(latestRates!!, currencyCode)
                    } else {
                        _ratesList.value = rateListFromDBToRatesList(rateListFromDB, currencyCode)
                    }
                } else {
                    _status.value = RevolutApiStatus.ERROR
                    _ratesList.value = ArrayList()
                }
            } else {
                insertOrUpdateRatesToDatabase(latestRates, rateListFromDB)
            }
        }
    }

    private suspend fun getRatesFromDatabase(): List<Rate> {
        return withContext(Dispatchers.IO) {
            val rateList = database.getAllRates()
            if(_ratesListFromDB.value == null && rateList.isNotEmpty()) {
                currencyCode = rateList[0].code
            }
            rateList
        }
    }

    private suspend fun insertOrUpdateRatesToDatabase(rates: Rates?, rateListFromDB: MutableList<Rate>) {
        rates?.let {
            withContext(Dispatchers.IO) {
                if (rateListFromDB.size != 0) {
                    database.get(currencyCode)?.let { rateInDB ->
                        rateInDB.rate = 1.0
                        Timber.d("Update: %d", database.update(rateInDB))
                    }
                } else {
                    Timber.d("Insert: %d", database.insert(Rate(code = currencyCode, rate = 1.0, flagImageResId = getFlagImageResId(currencyCode))))
                }
                Rates::class.memberProperties.forEach { member ->
                    val rate = member.get(rates) as Double?
                    if (rate != null) {
                        if (rateListFromDB.size != 0) {
                            database.get(member.name)?.let { rateInDB ->
                                rateInDB.rate = rate
                                Timber.d("Update: %d", database.update(rateInDB))
                            }
                        } else {
                            Timber.d("Insert: %d", database.insert(Rate(code = member.name, rate = rate, flagImageResId = getFlagImageResId(member.name))))
                        }
                    }
                }
            }
        }
    }

    private fun ratesToRateList(rates: Rates, baseRate: String): ArrayList<Rate> {
        val rateList: ArrayList<Rate> = ArrayList()
        rateList.add(Rate(code = baseRate, rate = currencyRate, flagImageResId = getFlagImageResId(baseRate)))
        Rates::class.memberProperties.forEach { member ->
            val rate = member.get(rates) as Double?
            if (rate != null) {
                rateList.add(Rate(code = member.name, rate = rate * currencyRate, flagImageResId = getFlagImageResId(member.name)))
            }
        }
        return rateList
    }

    private fun rateListFromDBToRatesList(rateListFromDB: MutableList<Rate>, baseCode: String): ArrayList<Rate> {
        Timber.d("basecode: $baseCode")
        val updatedRateList: ArrayList<Rate> = ArrayList()
        updatedRateList.add(Rate(code = baseCode, rate = currencyRate, flagImageResId = getFlagImageResId(baseCode)))
        for (rate in rateListFromDB) {
            if(rate.code == baseCode) {
                continue
            }
            updatedRateList.add(Rate(code = rate.code, rate = rate.rate * currencyRate, flagImageResId = getFlagImageResId(rate.code)))
        }
        return updatedRateList
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

    @ObsoleteCoroutinesApi
    fun onUpdateRates(rateList: MutableList<Rate>, moveToTop: Boolean) {
        if(!networkError) {
            Toast.makeText(app.applicationContext, "Please connect to the internet to get the latest rate", Toast.LENGTH_SHORT).show()
        } else if (rateList.size > 0) {
            currencyCode = rateList[0].code
            currencyRate = rateList[0].rate
            if (moveToTop) {
                viewModelJob.cancel()
                getLatestRates(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}