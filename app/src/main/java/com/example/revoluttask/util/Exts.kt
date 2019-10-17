package com.example.revoluttask.util

import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.revoluttask.ViewModelFactory
import com.example.revoluttask.model.RevolutDatabase

fun Fragment.getViewModelFactory(): ViewModelFactory {
    val application = requireNotNull(activity).application
    val dataSource = RevolutDatabase.getInstance(application).revolutDatabaseDao
    return ViewModelFactory(dataSource, application)
}

fun EditText.placeCursorToProperDigitPosition() {
    this.setSelection(this.text.split(".")[0].length)
}