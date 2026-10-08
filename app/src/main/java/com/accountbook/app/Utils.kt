package com.accountbook.app

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Double.money(): String = "₹" + String.format(Locale.US, "%,.2f", this)

fun Long.dateStr(): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))
