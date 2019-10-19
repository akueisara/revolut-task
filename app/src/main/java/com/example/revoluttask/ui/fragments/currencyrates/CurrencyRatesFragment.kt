package com.example.revoluttask.ui.currencyrates

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.revoluttask.databinding.FragmentCurrencyRatesBinding
import com.example.revoluttask.utils.getViewModelFactory
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.revoluttask.network.RevolutApiStatus
import kotlinx.coroutines.ObsoleteCoroutinesApi

class CurrencyRatesFragment : Fragment() {

    private lateinit var viewDataBinding: FragmentCurrencyRatesBinding

    private val viewModel by viewModels<CurrencyRatesViewModel> { getViewModelFactory() }

    private lateinit var currencyRatesadapter: CurrencyRatesAdapter

    @ObsoleteCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewDataBinding = FragmentCurrencyRatesBinding.inflate(inflater)

        viewDataBinding.setLifecycleOwner(this)

        viewDataBinding.viewModel = viewModel

        currencyRatesadapter = CurrencyRatesAdapter(context!!, mutableListOf(), RateClickListener { rateList, moveToTop ->
            viewModel.onUpdateRates(rateList, moveToTop)
        })

        viewDataBinding.ratesListRecyclerView.adapter = currencyRatesadapter
        (viewDataBinding.ratesListRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        viewModel.status.observe(this, Observer {
            it?.let {
                if(it == RevolutApiStatus.LOADING) {
                    currencyRatesadapter.updateConnectStatus(true)
                } else if(it == RevolutApiStatus.DONE_WITHOUT_CONNECTION || it == RevolutApiStatus.ERROR) {
                    currencyRatesadapter.updateConnectStatus(false)
                }
            }
        })

        viewModel.ratesListFromDB.observe(this, Observer {
            if(it != null) {
                viewModel.onGetRateListFromDBorResponse(it)
            }
        })

        viewModel.rateList.observe(this, Observer {
            if(it != null) {
                currencyRatesadapter.updateData(it)
            }
        })

        return viewDataBinding.root
    }


    @ObsoleteCoroutinesApi
    override fun onResume() {
        super.onResume()
        viewModel.resumeJob()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseJob()
    }
}