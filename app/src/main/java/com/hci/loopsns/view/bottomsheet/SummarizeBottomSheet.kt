package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.hci.loopsns.R
import com.hci.loopsns.network.IntentionSubjectResponse
import com.hci.loopsns.network.LocalitiesResponse
import com.hci.loopsns.network.NetworkManager
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import kotlin.reflect.KFunction1


class SummarizeBottomSheet(private val reportDownloadAction: KFunction1<Call<ResponseBody>, Unit>) : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var localities: Map<String, Map<String, List<String>>>
    private lateinit var cat1: List<String>
    private lateinit var cat2: List<String>

    private var cat1s: ArrayList<String> = ArrayList()
    private var cat2s: ArrayList<String> = ArrayList()

    private lateinit var cat1btn: AppCompatButton
    private lateinit var cat2btn: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_report, container, false)

        cat1btn = viewOfLayout.findViewById<AppCompatButton>(R.id.cat1)
        cat2btn = viewOfLayout.findViewById<AppCompatButton>(R.id.cat2)

        fetchCustomClaims()

        cat1btn.setOnClickListener(this)
        cat2btn.setOnClickListener(this)

        viewOfLayout.findViewById<Button>(R.id.report_download_btn).setOnClickListener(this)

        NetworkManager.apiService.getAllLocalities().enqueue(object : Callback<LocalitiesResponse> {
            override fun onResponse(
                call: Call<LocalitiesResponse>,
                response: Response<LocalitiesResponse>
            ) {
                if(!response.isSuccessful) {
                    Toast.makeText(requireContext(), "지역 정보를 받아올 수 없습니다.", Toast.LENGTH_LONG).show()
                    dismiss()
                    return
                }

                localities = response.body()!!.data
            }

            override fun onFailure(p0: Call<LocalitiesResponse>, err: Throwable) {
                Log.e("Get Localities Error", err.toString())
                Toast.makeText(requireContext(), "지역 정보를 받아올 수 없습니다.", Toast.LENGTH_LONG).show()
                dismiss()
            }

        })


        NetworkManager.apiService.getIntentionsSubjects(Locale.getDefault().language).enqueue(object : Callback<IntentionSubjectResponse> {
            override fun onResponse(
                call: Call<IntentionSubjectResponse>,
                response: Response<IntentionSubjectResponse>
            ) {
                if(!response.isSuccessful) {
                    dismiss()
                    return
                }

                cat1 = response.body()!!.intentions.drop(1)
                cat2 = response.body()!!.subjects.drop(1)

                cat1s = ArrayList(cat1)
                cat2s = ArrayList(cat2)
            }

            override fun onFailure(p0: Call<IntentionSubjectResponse>, p1: Throwable) {
                dismiss()
            }
        })

        return viewOfLayout
    }

    fun fetchCustomClaims() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idTokenResult = task.result
                val claims = idTokenResult?.claims
                if (claims != null) {
                    requireView().findViewById<TextView>(R.id.manage_location).text = (claims["licenses"] as Map<*, *>)["region"] as String
                    return@addOnCompleteListener
                }
                requireView().findViewById<TextView>(R.id.manage_location).text = "알 수 없는 지역"
            } else {
                requireView().findViewById<TextView>(R.id.manage_location).text = "오류 지역"
            }
        }
    }

    override fun onClick(v: View) {
        if(childFragmentManager.findFragmentByTag("SummarizeSelectBottomSheet") != null) return

        when(v.id) {
            R.id.cat1 -> {
                SummarizeSelectCategotyBottomSheet(cat1, cat1s, ::onCat1Changed)
                    .show(childFragmentManager, "SummarizeSelectBottomSheet")
            }
            R.id.cat2 -> {
                SummarizeSelectCategotyBottomSheet(cat2, cat2s, ::onCat2Changed)
                    .show(childFragmentManager, "SummarizeSelectBottomSheet")
            }
            R.id.report_download_btn -> {
                reportDownloadAction(
                    NetworkManager.apiService.downloadReport(
                        cat1s,
                        cat2s,
                    )
                )
                dismiss()
            }
        }
    }

    fun onCat1Changed(text: String, checked: Boolean) {
        if(checked) {
            cat1s.add(text)
        } else {
            cat1s.remove(text)
        }

        if(cat1s.size == cat1.size) {
            cat1btn.text = "전체 의도"
        } else if(cat1s.size > 1) {
            cat1btn.text = buildString {
                append(cat1s[0])
                append(" 외 ")
                append(cat1s.size - 1)
                append("개")
            }
        } else if(cat1s.size == 1){
            cat1btn.text = cat1s[0]
        } else {
            Toast.makeText(requireContext(), "의도를 선택하지 않고 보고서 다운로드 시 전체 의도가 요청됩니다.", Toast.LENGTH_SHORT).show()
            cat1btn.text = "전체 의도"
        }
    }

    fun onCat2Changed(text: String, checked: Boolean) {
        if(checked) {
            cat2s.add(text)
        } else {
            cat2s.remove(text)
        }

        if(cat2s.size == cat2.size) {
            cat2btn.text = "전체 주제"
        } else if(cat2s.size > 1) {
            cat2btn.text = buildString {
                append(cat2s[0])
                append(" 외 ")
                append(cat2s.size - 1)
                append("개")
            }
        } else if(cat2s.size == 1){
            cat2btn.text = cat2s[0]
        } else {
            Toast.makeText(requireContext(), "주제를 선택하지 않고 보고서 다운로드 시 전체 주제가 요청됩니다.", Toast.LENGTH_SHORT).show()
            cat2btn.text = "전체 주제"
        }
    }

}