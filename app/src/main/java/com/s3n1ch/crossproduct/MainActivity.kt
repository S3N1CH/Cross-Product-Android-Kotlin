package com.s3n1ch.crossproduct

import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.textfield.TextInputEditText
import com.viniciusmo.keyboardvisibility.keyboard
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_numeral_result_window.*
import kotlinx.android.synthetic.main.fragment_vector_result_window.*

class MainActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setAppTheme()
        startAnim(calculateButton, clearAllButton, resources.displayMetrics.widthPixels)
        hardHideResultWindow()
        setMainBlockResponse()
        setButtonsFunctionality(createCoordInputsHashMap(), createResultEditTextsHashMap())
    }

    override fun onStart() {
        super.onStart()
        Log.i("MyLOG", "onStart")
    }

    override fun onResume() {
        super.onResume()
        startAnim(calculateButton, clearAllButton, resources.displayMetrics.widthPixels)
        Log.i("MyLOG", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i("MyLOG", "onPause")
        hideResultWindow()
    }

    override fun onStop() {
        super.onStop()
        Log.i("MyLOG", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MyLOG", "onDestroy")
    }

    private fun startAnim(
        leftButton: Button, rightButton: Button,
        displayWidth: Int, start: Boolean = true
    ) {
        val valueAnimator: ValueAnimator = if (start) {  // up
            ValueAnimator.ofFloat(-displayWidth.toFloat(), 0f)
        } else { // down
            ValueAnimator.ofFloat(0f, -displayWidth.toFloat())
        }
        valueAnimator.addUpdateListener { it ->
            val value: Float = it.animatedValue as Float
            rightButton.translationX = value
            leftButton.translationX = -value
        }
        valueAnimator.interpolator = AnticipateOvershootInterpolator()
        valueAnimator.duration = 1600
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
        val valueAnimator: ValueAnimator = if (hide) {  // up
            ValueAnimator.ofFloat(-310f, 0f)
        } else { // down
            ValueAnimator.ofFloat(0f, -310f)
        }
        valueAnimator.addUpdateListener { it ->
            val value: Float = it.animatedValue as Float
            mainBlock.translationY = value
        }
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 200
        valueAnimator.start()
    }

    private fun setButtonsFunctionality(
        textInputEditTexts: HashMap<String, TextInputEditText>,
        uResultEditTexts: HashMap<String, TextInputEditText>
    ) {
        calculateButton.setOnClickListener {
            try {
                if (calculateButton.text == getString(R.string.calculate)) {
                    keyboard {
                        if (isKeyboardOpen()) {
                            a33.onEditorAction(EditorInfo.IME_ACTION_DONE)
                            a33.requestFocus()
                        }
                    }
                    CalculatorAlgorithm(this, uResultEditTexts, textInputEditTexts)
                } else {
                    val toCopy = getCurrentResultWindowText(CalculatorAlgorithm.currentResultWindow)
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText("LABELTEST", toCopy)
                    clipboard.setPrimaryClip(clip)
                    showToast("Copied")
                }
            } catch (e: Exception) {
                Log.w("EXCEPTION_OCCURED", e.toString())
            }
        }
        clearAllButton.setOnClickListener {
            try {
                if (clearAllButton.text == getString(R.string.clear_all)) {
                    clearAll()
                }
                a11.requestFocus()
                hideResultWindow()
            } catch (e: Exception) {
                Log.w("EXCEPTION_OCCURED", e.toString())
            }
        }
        // Switch day/night mode
        swtch_theme_btn.setOnClickListener { switchAppTheme() }
    }

    private fun getCurrentResultWindowText(currentResultWindow: Int): String {
        return if (currentResultWindow == 1) {
            """
                ${getString(R.string.solution)}
                ${uNExpressionEditText.text} ${uNAnswerEditText.text}.
            """.trimIndent()
        } else {
            """
                ${getString(R.string.solution)}
                ${getString(R.string.ux)} (${uXResultEditText.text});
                ${getString(R.string.uy)} (${uYResultEditText.text});
                ${getString(R.string.uz)} (${uZResultEditText.text}).
            """.trimIndent()
        }
    }

    private fun hideResultWindow() {
        val fragTransaction = supportFragmentManager.beginTransaction()
        fragTransaction.setCustomAnimations(R.anim.show_result, R.anim.hide_result)
        fragTransaction.hide(fragmentVectorResultWindow)
        fragTransaction.hide(fragmentNumeralResultWindow)
        clearAllButton.text = getString(R.string.clear_all)
        calculateButton.text = getString(R.string.calculate)
        fragTransaction.addToBackStack(null)
        fragTransaction.commit()
    }

    private fun hardHideResultWindow() {
        val fragTransaction = supportFragmentManager.beginTransaction()
        fragTransaction.hide(fragmentVectorResultWindow)
        fragTransaction.hide(fragmentNumeralResultWindow)
        fragTransaction.commit()
    }

    private fun clearAll() {
        for (i in (1..3)) {
            for (j in (1..3)) {
                val cell = "a$i$j"
                val input = getTextInputEditText(cell)
                input.text = null
            }
        }
    }

    private fun createCoordInputsHashMap(): HashMap<String, TextInputEditText> {
        val textInputs = hashMapOf<String, TextInputEditText>()
        for (i in (1..3)) {
            for (j in (1..3)) {
                val cell = "a$i$j"
                val textInput = getTextInputEditText(cell)
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

    private fun getTextInputEditText(objId: String): TextInputEditText {
        return when (objId) {
            "a11" -> findViewById(R.id.a11)
            "a12" -> findViewById(R.id.a12)
            "a13" -> findViewById(R.id.a13)
            "a21" -> findViewById(R.id.a21)
            "a22" -> findViewById(R.id.a22)
            "a23" -> findViewById(R.id.a23)
            "a31" -> findViewById(R.id.a31)
            "a32" -> findViewById(R.id.a32)
            else -> findViewById(R.id.a33)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                showToastToExit()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showToastToExit() {
        when {
            doubleBackToExitPressedOnce -> {
                onBackPressed()
            }
            else -> {
                doubleBackToExitPressedOnce = true
                showToast(getString(R.string.back_again_to_exit))
                Handler(Looper.myLooper()!!).postDelayed(
                    { doubleBackToExitPressedOnce = false },
                    2000
                )
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isNightModeOn(): Boolean {
        val appSharedPreferences: SharedPreferences =
            getSharedPreferences("AppSharedPreferences", 0)
        return appSharedPreferences.getBoolean(
            "NightMode",
            false
        )  // Check night/day mode in internal storage
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