package com.example.revoluttask.util

import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.revoluttask.ViewModelFactory

fun Fragment.getViewModelFactory(): ViewModelFactory {
    val application = requireNotNull(activity).application
    return ViewModelFactory(application)
}

fun EditText.placeCursorToProperDigitPosition() {
    this.setSelection(this.text.split(".")[0].length)
}