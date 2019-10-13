package com.example.revoluttask.ui.currencyrates

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.revoluttask.databinding.ListRatesBinding
import com.example.revoluttask.model.Rate
import com.example.revoluttask.util.Converter
import timber.log.Timber

class CurrencyRatesAdapter(var rateList: MutableList<Rate>, val clickListener: RateClickListener) : RecyclerView.Adapter<CurrencyRatesAdapter.ViewHolder>()  {
    override fun getItemCount() = rateList.size

    inner class ViewHolder(private var binding: ListRatesBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(rate: Rate) {
            binding.rate = rate

            binding.rateItemLayout.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scrollToTop(layoutPosition)
                }
            }

            binding.currencyRateEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scrollToTop(layoutPosition)
                }
            }

            binding.currencyRateEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val text = s.toString()
                    val firstCurrency = rateList[0]

                    if (layoutPosition == 0 && text != String.format("%,.2f", firstCurrency.rate)) {

                        val baseCurrencyRate = if (text.isEmpty()) 1.0 else Converter.rateStringToDouble(text)

                        rateList.map { it.rate = (baseCurrencyRate.times(it.rate)) }

                        firstCurrency.rate = baseCurrencyRate

                        clickListener.onClick(firstCurrency)
                    }
                }
            })

            binding.currencyRateEditText.setSelection(binding.currencyRateEditText.text?.length ?: 0)

            binding.executePendingBindings()
        }
    }

    fun updateData(updatedList: MutableList<Rate>) {
        rateList = updatedList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListRatesBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rate = rateList[position]
        holder.bind(rate)
    }

     private fun scrollToTop(layoutPosition: Int) {
        if(layoutPosition > 0) {
            rateList.removeAt(layoutPosition).also {
                it.rate = 1.0
                rateList.add(0, it)
                clickListener.onClick(it)
            }
            notifyItemMoved(layoutPosition, 0)
        }
    }

}

class RateClickListener(val clickListener: (rate: Rate) -> Unit) {
    fun onClick(rate: Rate) = clickListener(rate)
}