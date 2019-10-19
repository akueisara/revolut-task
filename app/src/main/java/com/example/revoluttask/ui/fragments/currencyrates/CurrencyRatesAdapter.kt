package com.example.revoluttask.ui.currencyrates

import android.content.Context
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.revoluttask.databinding.ListRatesBinding
import com.example.revoluttask.model.Rate
import com.example.revoluttask.utils.Converter
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.example.revoluttask.utils.placeCursorToProperDigitPosition


class CurrencyRatesAdapter(val context: Context, var rateList: MutableList<Rate>, val clickListener: RateClickListener) : RecyclerView.Adapter<CurrencyRatesAdapter.ViewHolder>()  {
    private var APIConnected = true

    override fun getItemCount() = rateList.size

    inner class ViewHolder(private var binding: ListRatesBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(rate: Rate, position: Int) {
            binding.currencyRateEditText.isEnabled = !(position != 0 && !APIConnected)

            binding.rate = rate

            binding.rateItemLayout.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scrollToTop(false)
                }
            }

            binding.currencyRateEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scrollToTop(true)
                }
            }

            binding.currencyRateEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val text = s.toString()
                    val firstCurrency = rateList[0]

                    binding.currencyRateEditText.placeCursorToProperDigitPosition()

                    if (layoutPosition == 0 && text != String.format("%,.2f", firstCurrency.rate)) {

                        val baseCurrencyRate = if (text.isEmpty()) 1.0 else Converter.rateStringToDouble(text)

                        rateList.map { it.rate = (baseCurrencyRate.times(it.rate)) }

                        firstCurrency.rate = baseCurrencyRate

                        clickListener.onUpdateRates(rateList, false)
                    }
                }
            })

            binding.currencyRateEditText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE -> {
                            binding.rateItemLayout.clearFocus()
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.windowToken, 0)
                            return true
                        }
                    }
                    return false
                }
            })

            binding.currencyRateEditText.placeCursorToProperDigitPosition()
            binding.executePendingBindings()
        }

        private fun scrollToTop(keepFocus: Boolean) {
            if(APIConnected && layoutPosition > 0) {
                rateList.removeAt(layoutPosition).also {
                    it.rate = 1.0
                    rateList.add(0, it).let {
                        clickListener.onUpdateRates(rateList, true)
                    }
                }
                notifyItemMoved(layoutPosition, 0)
            }
            if(!keepFocus) {
                Handler().postDelayed({
                    binding.rateItemLayout.clearFocus()
                }, 100)
            }
        }
    }

    fun updateData(updatedList: MutableList<Rate>) {
        rateList = updatedList
        notifyDataSetChanged()
    }

    fun updateConnectStatus(connected: Boolean) {
        APIConnected = connected
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListRatesBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rate = rateList[position]
        holder.bind(rate, position)
    }

}

class RateClickListener(val clickListener: (rateList: MutableList<Rate>, moveToTop: Boolean) -> Unit) {
    fun onUpdateRates(rateList: MutableList<Rate>, moveToTop: Boolean) = clickListener(rateList, moveToTop)
}