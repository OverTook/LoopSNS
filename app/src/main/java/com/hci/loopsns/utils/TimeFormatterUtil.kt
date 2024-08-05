package com.hci.loopsns.utils

import com.yariksoffice.lingver.Lingver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun String.toDate(dateFormat: String = "yyyy-MM-dd HH:mm", timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date {
    val parser = SimpleDateFormat(dateFormat, Lingver.getInstance().getLocale())
    parser.timeZone = timeZone
    return parser.parse(this)!!
}

fun Date.formatTo(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
    val formatter = SimpleDateFormat(dateFormat, Lingver.getInstance().getLocale())
    formatter.timeZone = timeZone
    return formatter.format(this)
}