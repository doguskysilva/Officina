package com.doguskytech.officina.data

import androidx.annotation.StringRes
import com.doguskytech.officina.R

enum class Priority(@StringRes val labelRes: Int) {
    LOW(R.string.priority_low),
    MEDIUM(R.string.priority_medium),
    HIGH(R.string.priority_high),
}
