package com.example.revoluttask.ui.currencyrates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.revoluttask.model.Rate
import com.example.revoluttask.model.RevolutDatabaseDao
import com.example.revoluttask.network.RevolutApi
import com.example.revoluttask.network.RevolutApiStatus
import com.example.revoluttask.network.latestrate.Rates
import com.example.revoluttask.utils.CurrencyRatesUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import kotlin.reflect.full.memberProperties

class CurrencyRatesViewModel(val database: RevolutDatabaseDao, val app: Application) : AndroidViewModel(app) {

    private companion object {
        const val DEFAULT_CURRENCY_CODE = "EUR"
        const val DEFAULT_CURRENCY_RATE = 1.0
        const val GET_RATE_INTERNAL = 1_000L
    }

    private var currencyCode: String = DEFAULT_CURRENCY_CODE

    private var currencyRate: Double = DEFAULT_CURRENCY_RATE

    private lateinit var viewModelJob: Job

    private lateinit var coroutineScope: CoroutineScope

    private val _status = MutableLiveData<RevolutApiStatus>()

    val status: LiveData<RevolutApiStatus>
        get() = _status

    private val _ratesList = MutableLiveData<MutableList<Rate>>()

    val rateList: LiveData<MutableList<Rate>>
        get() = _ratesList

    private val _ratesListFromDB = MutableLiveData<MutableList<Rate>>()

    val ratesListFromDB: LiveData<MutableList<Rate>>
        get() = _ratesListFromDB

    private val _errorMessage = MutableLiveData<String>()

    val errorMessage: LiveData<String>
        get() = _errorMessage

    private var latestRates: Rates? = null

    private var apiConnection = false

    @ObsoleteCoroutinesApi
    private fun getLatestRates(showLoading: Boolean = true) {
        viewModelJob = Job()
        coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)
        coroutineScope.launch {
            if (showLoading) {
                _status.value = RevolutApiStatus.LOADING
            }
            val tickerChannel = ticker(delayMillis = GET_RATE_INTERNAL, initialDelayMillis = 0)
            for (event in tickerChannel) {
                try {
                    val getLatestRatesResponse = RevolutApi.retrofitService.getLatestRates(currencyCode).await()
                    _status.value = RevolutApiStatus.DONE
                    apiConnection = true
                    latestRates = getLatestRatesResponse.rates
                    _ratesList.value = CurrencyRatesUtil.ratesResponseToRateList(app.applicationContext, latestRates!!, currencyCode, currencyRate)
                    _ratesListFromDB.value = getRatesFromDatabase().toMutableList()
                } catch (e: Exception) {
                    _errorMessage.value = RevolutApi.APIError(e).message
                    apiConnection = false
                    _ratesListFromDB.value = getRatesFromDatabase().toMutableList()
                    tickerChannel.cancel()
                }
            }
        }
    }

    fun onGetRateListFromDBorResponse(rateListFromDB: MutableList<Rate>) {
        coroutineScope.launch {
            if (_status.value == RevolutApiStatus.DONE && apiConnection) {
                insertOrUpdateRatesToDB(latestRates, rateListFromDB)
            } else {
                if (rateListFromDB.size != 0) {
                    if(!apiConnection) {
                        _status.value = RevolutApiStatus.DONE_WITHOUT_CONNECTION
                    }
                    if (latestRates != null) {
                        _ratesList.value = CurrencyRatesUtil.ratesResponseToRateList(app.applicationContext, latestRates!!, currencyCode, currencyRate)
                    } else {
                        _ratesList.value = CurrencyRatesUtil.rateListFromDBToRateList(app.applicationContext, rateListFromDB, currencyCode, currencyRate)
                    }
                } else {
                    _status.value = RevolutApiStatus.ERROR
                    _ratesList.value = ArrayList()
                }
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

    private suspend fun insertOrUpdateRatesToDB(rates: Rates?, rateListFromDB: MutableList<Rate>) {
        rates?.let {
            withContext(Dispatchers.IO) {
                updateOrInsertRates(DEFAULT_CURRENCY_RATE, currencyCode, rateListFromDB)
                Rates::class.memberProperties.forEach { member ->
                    val rate = member.get(rates) as Double?
                    if (rate != null) {
                        updateOrInsertRates(rate, member.name, rateListFromDB)
                    }
                }
            }
        }
    }

    private fun updateOrInsertRates(currencyRate: Double, currencyCode: String, rateListFromDB: MutableList<Rate>) {
        if (rateListFromDB.size != 0) {
            database.get(currencyCode)?.let { rateInDB ->
                rateInDB.rate = currencyRate
                database.update(rateInDB)
            }
        } else {
            database.insert(Rate(code = currencyCode, rate = currencyRate, flagImageResId = CurrencyRatesUtil.getFlagImageResId(app.applicationContext, currencyCode)))
        }
    }

    @ObsoleteCoroutinesApi
    fun onUpdateRates(rateList: MutableList<Rate>, moveToTop: Boolean) {
        if (rateList.size > 0) {
            currencyCode = rateList[0].code
            currencyRate = rateList[0].rate
            if (moveToTop) {
                viewModelJob.cancel()
                getLatestRates(false)
            } else if(!apiConnection) {
                _ratesListFromDB.value = _ratesListFromDB.value
            }
        }
    }

    @ObsoleteCoroutinesApi
    fun resumeJob() {
        getLatestRates()
    }

    fun pauseJob() {
        viewModelJob.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}