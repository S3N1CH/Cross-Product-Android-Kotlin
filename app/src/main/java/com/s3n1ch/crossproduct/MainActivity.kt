package com.s3n1ch.crossproduct

import android.animation.ValueAnimator
import android.animation.ValueAnimator.ofFloat
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.textfield.TextInputEditText
import com.viniciusmo.keyboardvisibility.keyboard
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setAppTheme()
        startButtonsAnim()
        hardHideResultWindow()
        setMainBlockResponse()
        setButtonsFunctionality()
    }

    override fun onStart() {
        super.onStart()
        Log.i("LIFECYCLE_EVENT", "onStart")
    }

    override fun onResume() {
        super.onResume()
        startButtonsAnim()
        Log.i("LIFECYCLE_EVENT", "onResume")
    }

    override fun onPause() {
        super.onPause()
        hideResultWindow()
        Log.i("LIFECYCLE_EVENT", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i("LIFECYCLE_EVENT", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("LIFECYCLE_EVENT", "onDestroy")
    }

    private fun startButtonsAnim() {
        val valueAnimator: ValueAnimator = ofFloat(resources.displayMetrics.widthPixels.toFloat(), 0f)
        valueAnimator.addUpdateListener {
            val value: Float = it.animatedValue as Float
            calculateButton.translationX = value
            clearAllButton.translationX = -value
        }
        valueAnimator.interpolator = AnticipateOvershootInterpolator()
        valueAnimator.duration = 1200
        valueAnimator.start()
    }

    private fun setMainBlockResponse() {
        // If kb is closed mainBlock is centered; if opened it floats up
        keyboard {
            onOpened { mainBlockAnim(mainBlock, hide = false) }
            onClosed { mainBlockAnim(mainBlock, hide = true) }
        }
    }

    private fun mainBlockAnim(mainBlock: RelativeLayout, hide: Boolean = false) {
        val valueAnimator: ValueAnimator = if (hide) ofFloat(-310f, 0f) else ofFloat(0f, -310f)
        valueAnimator.addUpdateListener {
            val value: Float = it.animatedValue as Float
            mainBlock.translationY = value
        }
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 100
        valueAnimator.start()
    }

    private fun setButtonsFunctionality() {
        val textInputEditTexts = createCoordInputsHashMap()
        val uResultEditTexts = createResultEditTextsHashMap()

        calculateButton.setOnClickListener {
            try {
                if (CalculatorAlgorithm.isResultWindowOpened) copyData() else {
                    CalculatorAlgorithm(this, uResultEditTexts, textInputEditTexts)
                }
            } catch (e: Exception) {
                Log.w("EXCEPTION_CALCULATE", e.toString())
            }
        }
        clearAllButton.setOnClickListener {
            try {
                if (CalculatorAlgorithm.isResultWindowOpened) hideResultWindow() else clearAll()
                a11.requestFocus()
            } catch (e: Exception) {
                Log.w("EXCEPTION_CLEAR_ALL", e.toString())
            }
        }
        swtch_theme_btn.setOnClickListener { switchAppTheme() }  // Switch day/night mode
    }

    private fun copyData() {
        val toCopy = CalculatorAlgorithm.getCurrentResultWindowText(this)
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("", toCopy)
        clipboard.setPrimaryClip(clip)
        showToast(this, getString(R.string.copied))
    }

    private fun hideResultWindow() {
        if (CalculatorAlgorithm.isResultWindowOpened) {
            val fragment = if (CalculatorAlgorithm.currentResultWindow == 0) fragmentVectorResultWindow else fragmentNumeralResultWindow
            val fragTransaction = supportFragmentManager.beginTransaction()
            fragTransaction.setCustomAnimations(R.anim.show_result, R.anim.hide_result)
            fragTransaction.hide(fragment)
            fragTransaction.commit()
            clearAllButton.text = getString(R.string.clear_all)
            calculateButton.text = getString(R.string.calculate)
            CalculatorAlgorithm.isResultWindowOpened = false
        }
    }

    private fun hardHideResultWindow() {
        val fragTransaction = supportFragmentManager.beginTransaction()
        fragTransaction.hide(fragmentVectorResultWindow)
        fragTransaction.hide(fragmentNumeralResultWindow)
        fragTransaction.commit()
        CalculatorAlgorithm.isResultWindowOpened = false
    }

    private fun clearAll() {
        for (i in 1..3) {
            for (j in 1..3) {
                getTextInputEditText(this, "a$i$j").text = null
            }
        }
    }

    private fun createCoordInputsHashMap(): HashMap<String, TextInputEditText> {
        val textInputs = hashMapOf<String, TextInputEditText>()
        for (i in (1..3)) {
            for (j in (1..3)) {
                val cell = "a$i$j"
                val textInput = getTextInputEditText(this, "a$i$j")
                textInputs[cell] = textInput
            }
        }
        return textInputs
    }

    private fun createResultEditTextsHashMap(): HashMap<String, TextInputEditText> {
        val resultEditTextsHashMap = hashMapOf<String, TextInputEditText>()
        resultEditTextsHashMap["uXResultEditText"] = findViewById(R.id.uXResultEditText)
        resultEditTextsHashMap["uYResultEditText"] = findViewById(R.id.uYResultEditText)
        resultEditTextsHashMap["uZResultEditText"] = findViewById(R.id.uZResultEditText)
        resultEditTextsHashMap["uNExpressionEditText"] = findViewById(R.id.uNExpressionEditText)
        resultEditTextsHashMap["uNAnswerEditText"] = findViewById(R.id.uNAnswerEditText)
        return resultEditTextsHashMap
    }

    override fun onBackPressed() {
        when (doubleBackToExitPressedOnce) {
            false -> {
                doubleBackToExitPressedOnce = true
                hideResultWindow()
                showToast(this, getString(R.string.back_again_to_exit))
                Handler(Looper.myLooper()!!).postDelayed({ doubleBackToExitPressedOnce = false },2000)
            }
            else -> super.onBackPressed()
        }
    }

    private fun isNightModeOn(): Boolean {
        val appSharedPreferences: SharedPreferences =
            getSharedPreferences("AppSharedPreferences", 0)
        return appSharedPreferences.getBoolean("NightMode", false)  // Check night/day mode in internal storage
    }

    private fun setAppTheme() {
        if (isNightModeOn()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun switchAppTheme() {
        val appSharedPreferences: SharedPreferences =
            getSharedPreferences("AppSharedPreferences", 0)
        val sharedPrefsEdit: SharedPreferences.Editor = appSharedPreferences.edit()
        if (isNightModeOn()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPrefsEdit.putBoolean("NightMode", false)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPrefsEdit.putBoolean("NightMode", true)
        }
        sharedPrefsEdit.apply()
    }
}