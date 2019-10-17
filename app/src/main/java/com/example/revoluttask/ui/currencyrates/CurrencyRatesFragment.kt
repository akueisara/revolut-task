package com.example.revoluttask.ui.currencyrates

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.revoluttask.databinding.FragmentCurrencyRatesBinding
import com.example.revoluttask.util.getViewModelFactory
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.revoluttask.network.RevolutApiStatus
import kotlinx.coroutines.ObsoleteCoroutinesApi

class CurrencyRatesFragment : Fragment() {

    private lateinit var viewDataBinding: FragmentCurrencyRatesBinding

    private val viewModel by viewModels<CurrencyRatesViewModel> { getViewModelFactory() }

    private lateinit var currencyRatesadapter: CurrencyRatesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewDataBinding = FragmentCurrencyRatesBinding.inflate(inflater)

        viewDataBinding.setLifecycleOwner(this)

        viewDataBinding.viewModel = viewModel

        currencyRatesadapter = CurrencyRatesAdapter(mutableListOf(), RateClickListener { rateList, moveToTop ->
            viewModel.onUpdateRates(rateList, moveToTop)
        })

        viewDataBinding.ratesListRecyclerView.adapter = currencyRatesadapter
        (viewDataBinding.ratesListRecyclerView.getItemAnimator() as SimpleItemAnimator).supportsChangeAnimations = false

        viewModel.status.observe(this, Observer {
            it?.let {
               when(it) {
                   RevolutApiStatus.LOADING -> {
                       currencyRatesadapter.updateConnectStatus(true)
                       viewDataBinding.loadingProgressBar.visibility = View.VISIBLE
                       viewDataBinding.ratesListRecyclerView.visibility = View.GONE
                       viewDataBinding.errorLayout.visibility = View.GONE
                       viewDataBinding.noInternetBottomLayout.visibility = View.GONE
                   }
                   RevolutApiStatus.DONE -> {
                       viewDataBinding.loadingProgressBar.visibility = View.GONE
                       viewDataBinding.ratesListRecyclerView.visibility = View.VISIBLE
                   }
                   RevolutApiStatus.LOCALDATA -> {
                       currencyRatesadapter.updateConnectStatus(false)
                       viewDataBinding.loadingProgressBar.visibility = View.GONE
                       viewDataBinding.ratesListRecyclerView.visibility = View.VISIBLE
                       viewDataBinding.noInternetBottomLayout.visibility = View.VISIBLE
                   }
                   RevolutApiStatus.ERROR -> {
                       currencyRatesadapter.updateConnectStatus(false)
                       viewDataBinding.loadingProgressBar.visibility = View.GONE
                       viewDataBinding.errorLayout.visibility = View.VISIBLE
                   }
               }
            }
        })

        viewModel.ratesListFromDB.observe(this, Observer {
            if(it != null) {
                viewModel.updateRateList(it)
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