package com.hci.loopsns

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.network.TermsOfAnyResponse
import com.hci.loopsns.storage.SettingManager
import com.skydoves.androidveil.VeilLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale


class SearchResultActivity : AppCompatActivity(), View.OnClickListener,
    TextView.OnEditorActionListener {

    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animationInit()
        setContentView(R.layout.activity_search_result)

        searchEditText = findViewById(R.id.filter)
        searchEditText.setOnEditorActionListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            searchEditText.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
        } else {
            searchEditText.requestFocus()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        lifecycleScope.launch {
            delay(300)
            //searchEditText.requestFocus()
            //val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            //imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }
//        searchEditText.requestFocus()
//        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingManager.getInstance().getCurrentLocaleContext(base))
    }

    fun animationInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    override fun finish() {
        super.finish()
        Log.e("SDK", Build.VERSION.SDK_INT.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            overridePendingTransition(0, 0)
        }
    }

    override fun onClick(view: View?) {
        if(view == null) return

        when(view.id) {
            R.id.backButton -> {
                finish()
            }
        }
    }

    override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
        return true
    }
}