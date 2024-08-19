package com.hci.loopsns.view.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hci.loopsns.R
import com.hci.loopsns.network.LocalitiesResponse
import com.hci.loopsns.network.NetworkManager
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.reflect.KFunction1


class SummarizeBottomSheet(private val reportDownloadAction: KFunction1<Call<ResponseBody>, Unit>) : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var localities: Map<String, Map<String, List<String>>>
    private lateinit var cat1: List<String>
    private lateinit var cat2: List<String>

    private var cat1s: ArrayList<String> = ArrayList()
    private var cat2s: ArrayList<String> = ArrayList()
    private var locality1: String = ""
    private var locality2: String = ""
    private var locality3: String = ""

    private lateinit var cat1btn: AppCompatButton
    private lateinit var cat2btn: AppCompatButton
    private lateinit var loc1btn: AppCompatButton
    private lateinit var loc2btn: AppCompatButton
    private lateinit var loc3btn: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewOfLayout = inflater.inflate(R.layout.bottom_sheet_report, container, false)

        cat1btn = viewOfLayout.findViewById<AppCompatButton>(R.id.cat1)
        cat2btn = viewOfLayout.findViewById<AppCompatButton>(R.id.cat2)
        loc1btn = viewOfLayout.findViewById<AppCompatButton>(R.id.location1)
        loc2btn = viewOfLayout.findViewById<AppCompatButton>(R.id.location2)
        loc3btn = viewOfLayout.findViewById<AppCompatButton>(R.id.location3)

        cat1btn.setOnClickListener(this)
        cat2btn.setOnClickListener(this)
        loc1btn.setOnClickListener(this)
        loc2btn.setOnClickListener(this)
        loc3btn.setOnClickListener(this)

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

        cat1 = resources.getStringArray(R.array.categories1_array).drop(1)
        cat2 = resources.getStringArray(R.array.categories2_array).drop(1)

        cat1s = ArrayList(cat1)
        cat2s = ArrayList(cat2)

        return viewOfLayout
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
            R.id.location1 -> {
                SummarizeSelectLocationBottomSheet(arrayOf("전체 시/도") + localities.keys.toTypedArray(), ::onLocality1Changed)
                    .show(childFragmentManager, "SummarizeSelectBottomSheet")
            }
            R.id.location2 -> {
                if(locality1.isBlank()) {
                    Toast.makeText(requireContext(), "상위 지역이 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                SummarizeSelectLocationBottomSheet(arrayOf("전체 시/군/구") + localities[locality1]!!.keys.toTypedArray(), ::onLocality2Changed)
                    .show(childFragmentManager, "SummarizeSelectBottomSheet")
            }
            R.id.location3 -> {
                if(locality2.isBlank()) {
                    Toast.makeText(requireContext(), "상위 지역이 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    return
                }

                SummarizeSelectLocationBottomSheet(arrayOf("전체 읍/면/동") + localities[locality1]!![locality2]!!.toTypedArray(), ::onLocality3Changed)
                    .show(childFragmentManager, "SummarizeSelectBottomSheet")
            }
            R.id.report_download_btn -> {
                reportDownloadAction(
                    NetworkManager.apiService.downloadReport(
                        cat1s,
                        cat2s,
                        locality1,
                        locality2,
                        locality3
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

    fun onLocality1Changed(text: String) {
        if(locality1 != text) {
            locality2 = ""
            locality3 = ""
            loc2btn.text = "전체 시/군/구"
            loc3btn.text = "전체 읍/면/동"
        }

        if(text.contains("전체")) {
            locality1 = ""
            loc1btn.text = "전체 시/도"
            return
        }
        locality1 = text
        loc1btn.text = text
    }

    fun onLocality2Changed(text: String) {
        if(locality2 != text) {
            locality3 = ""
            loc3btn.text = "전체 읍/면/동"
        }

        if(text.contains("전체")) {
            locality2 = ""
            loc2btn.text = "전체 시/군/구"
            return
        }
        locality2 = text
        loc2btn.text = text
    }

    fun onLocality3Changed(text: String) {
        if(text.contains("전체")) {
            locality3 = ""
            loc3btn.text = "전체 읍/면/동"
            return
        }
        locality3 = text
        loc3btn.text = text
    }
}