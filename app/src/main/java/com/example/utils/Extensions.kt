package com.example.utils

import java.text.DecimalFormat

fun Long.toPersianCurrency(): String {
    val formatter = DecimalFormat("#,###")
    var formatted = formatter.format(this)
    // Convert English digits to Persian
    val persianDigits = arrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    for (i in 0..9) {
        formatted = formatted.replace(i.toString().toCharArray()[0], persianDigits[i])
    }
    return "$formatted تومان"
}

fun Int.toPersianNumber(): String {
    var formatted = this.toString()
    val persianDigits = arrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    for (i in 0..9) {
        formatted = formatted.replace(i.toString().toCharArray()[0], persianDigits[i])
    }
    return formatted
}

fun String.toPersianNumber(): String {
    var formatted = this
    val persianDigits = arrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    for (i in 0..9) {
        formatted = formatted.replace(i.toString().toCharArray()[0], persianDigits[i])
    }
    return formatted
}
