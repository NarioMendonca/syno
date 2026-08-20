package dev.nario.syno.utils

import android.util.Patterns
fun CharSequence?.isEmailInvalid(): Boolean {
    val isInvalid = this.isNullOrEmpty() || !Patterns.EMAIL_ADDRESS.matcher(this).matches()
    return isInvalid
}
