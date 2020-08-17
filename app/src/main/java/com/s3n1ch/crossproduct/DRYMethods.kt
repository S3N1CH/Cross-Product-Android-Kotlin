package com.s3n1ch.crossproduct

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

fun getTextInputEditText(ctx: MainActivity, objId: String): TextInputEditText {
    return when (objId) {
        "a11" -> ctx.findViewById(R.id.a11)
        "a12" -> ctx.findViewById(R.id.a12)
        "a13" -> ctx.findViewById(R.id.a13)
        "a21" -> ctx.findViewById(R.id.a21)
        "a22" -> ctx.findViewById(R.id.a22)
        "a23" -> ctx.findViewById(R.id.a23)
        "a31" -> ctx.findViewById(R.id.a31)
        "a32" -> ctx.findViewById(R.id.a32)
        else -> ctx.findViewById(R.id.a33)
    }
}

var isToastShown = false
fun showToast(ctx: MainActivity, message: String) {
    if (!isToastShown) {
        isToastShown = true
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
        Handler(Looper.myLooper()!!).postDelayed({ isToastShown = false },2000)
    }
}