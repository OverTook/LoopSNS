package com.hci.loopsns

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hci.loopsns.network.NetworkManager
import com.hci.loopsns.network.TermsOfAnyResponse
import com.hci.loopsns.storage.SettingManager
import com.skydoves.androidveil.VeilLayout
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale


class TermsActivity : AppCompatActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animationInit()
        setContentView(R.layout.activity_terms)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener(this)
        findViewById<VeilLayout>(R.id.veilLayout).veil()
        requestDocument()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(SettingManager.getInstance().getCurrentLocaleContext(base))
    }

    fun requestDocument() {
        var request: Call<TermsOfAnyResponse>? = null
        when(intent.getStringExtra("type") ?: "null") {
            "use" -> {
                findViewById<TextView>(R.id.activity_name).text = getString(R.string.terms_of_use)
                request = NetworkManager.apiService.getTermsOfUse(Locale.getDefault().language)
            }
            "information" -> {
                findViewById<TextView>(R.id.activity_name).text =
                    getString(R.string.terms_of_information)
                request = NetworkManager.apiService.getTermsOfInformation(Locale.getDefault().language)
            }
            "faq" -> {
                findViewById<TextView>(R.id.activity_name).text = getString(R.string.terms_of_faq)
                request = NetworkManager.apiService.getFAQ(Locale.getDefault().language)
            }
            else -> {
                Toast.makeText(this, getString(R.string.invalid_request), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        request!!.enqueue(object : Callback<TermsOfAnyResponse> {
            override fun onResponse(
                call: Call<TermsOfAnyResponse>,
                response: Response<TermsOfAnyResponse>
            ) {
                if(!response.isSuccessful) {
                    Toast.makeText(this@TermsActivity, getString(R.string.error_getting_terms), Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                val result = response.body()!!
                findViewById<VeilLayout>(R.id.veilLayout).unVeil()
                findViewById<TextView>(R.id.body).text = Html.fromHtml(result.data, Html.FROM_HTML_MODE_COMPACT)
                if(result.mail == null) {
                    findViewById<TextView>(R.id.contact).visibility = View.GONE
                    return
                }
                findViewById<TextView>(R.id.contact).text = result.mail
            }

            override fun onFailure(p0: Call<TermsOfAnyResponse>, p1: Throwable) {
                Toast.makeText(this@TermsActivity, getString(R.string.error_getting_terms), Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    fun animationInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.enter_from_right, R.anim.exit_to_left)
        } else {
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.enter_from_left, R.anim.exit_to_right)
        } else {
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
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
}