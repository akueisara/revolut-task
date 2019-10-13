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

class CurrencyRatesFragment : Fragment() {

    private lateinit var viewDataBinding: FragmentCurrencyRatesBinding

    private val viewModel by viewModels<CurrencyRatesViewModel> { getViewModelFactory() }

    private lateinit var currencyRatesadapter: CurrencyRatesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewDataBinding = FragmentCurrencyRatesBinding.inflate(inflater)

        viewDataBinding.setLifecycleOwner(this)

        viewDataBinding.viewModel = viewModel

        currencyRatesadapter = CurrencyRatesAdapter(mutableListOf(), RateClickListener {
            viewModel.onUpdateRate(it)
        })

        viewDataBinding.ratesListRecyclerView.adapter = currencyRatesadapter
        (viewDataBinding.ratesListRecyclerView.getItemAnimator() as SimpleItemAnimator).supportsChangeAnimations = false

        viewModel.rateList.observe(this, Observer {
            currencyRatesadapter.updateData(it)
        })

        return viewDataBinding.root
    }


    override fun onResume() {
        super.onResume()
        viewModel.resumeJob()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseJob()
    }
}