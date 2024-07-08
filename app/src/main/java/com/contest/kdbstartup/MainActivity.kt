package com.contest.kdbstartup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.contest.kdbstartup.network.AccountCreateResponse
import com.contest.kdbstartup.network.NetworkManager
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.vectormap.KakaoMapSdk
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInButton: SignInButton
    private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        KakaoMapSdk.init(applicationContext, "df17ea99e1579611972ffbb1ff069e51");
        KakaoSdk.init(applicationContext, "df17ea99e1579611972ffbb1ff069e51");
        //Log.e("KeyHash", Utility.getKeyHash(this))

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val intent = Intent(
            applicationContext,
            MapOverviewActivity::class.java
        )
        startActivity(intent)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = FirebaseAuth.getInstance()

        googleSignInButton = findViewById<SignInButton>(R.id.google_sign_btn)
        googleSignInButton.setOnClickListener {
            signIn()
        }

        val kakaoSignInButton = findViewById<Button>(R.id.kakao_sign_btn)
        kakaoSignInButton.setOnClickListener {
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e("Kakao Login", "카카오계정으로 로그인 실패", error)
                } else if (token != null) {
                    Log.i("Kakao Login", "카카오계정으로 로그인 성공 ${token.accessToken}")

                    //아래처럼 조작이 가능해진다는 문제점을 해결
                    //mAuth!!.createUserWithEmailAndPassword("fake@fake.com", "fakepassword")

                    retrieveCustomToken("kakao", token.accessToken)
                }
            }

            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(applicationContext)) {
                UserApiClient.instance.loginWithKakaoTalk(applicationContext) { token, error ->
                    if (error != null) {
                        Log.e("Kakao Login", "카카오톡으로 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(applicationContext, callback = callback)
                    } else if (token != null) {
                        Log.i("Kakao Login", "카카오톡으로 로그인 성공 ${token.accessToken}")
                        retrieveCustomToken("kakao", token.accessToken)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(applicationContext, callback = callback)
            }
        }
    }



    fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            //TODO 7월 9일 구글 로그인 인증 방식 다양화 필요
            is PasswordCredential -> {
                val username = credential.id
                val password = credential.password

                Log.e("Unsupported Credential", buildString {
                    append(username)
                    append(" : ")
                    append(password)
                })
            }

            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                        retrieveCustomToken("google", googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("Google Login Error", "Received an invalid google id token response", e)
                    }
                } else {
                    Log.e("Google Login Error", "Unexpected type of credential")
                }
            }

            else -> {
                Log.e("Google Login Error", "Unexpected type of credential")
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        if (isUserNonNull()) {
            updateUI()
        }
    }

    private fun isUserNonNull(): Boolean {
        return mAuth!!.currentUser != null
    }

    private fun updateUI() {
        //로그인이 이미 되어있으므로 TODO
    }

    private fun signIn() {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.google_sdk_id))
            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(applicationContext)

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = applicationContext,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Snackbar.make(findViewById(R.id.main), "인증 절차에 실패했습니다.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun retrieveCustomToken(platform: String, token: String) {
        NetworkManager.apiService.createAccount(platform, token).enqueue(object :
            Callback<AccountCreateResponse> {
            override fun onResponse(call: Call<AccountCreateResponse>, response: Response<AccountCreateResponse>) {
                if(!response.isSuccessful) {
                    Snackbar.make(findViewById(R.id.main), "회원 정보 생성에 실패하였습니다.", Snackbar.LENGTH_SHORT).show();
                    return
                }

                if(!response.body()!!.success) {
                    Log.e("Error", response.body()!!.msg)
                    Snackbar.make(findViewById(R.id.main), "회원 정보 생성에 실패하였습니다.", Snackbar.LENGTH_SHORT).show();
                    return
                }
                val customToken = response.body()!!.token

                mAuth!!.signInWithCustomToken(customToken).addOnCompleteListener(
                    this@MainActivity
                ) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth!!.currentUser

                        //토큰을 통해 NetworkManager 체인 연결
                        user!!.getIdToken(true)
                            .addOnCompleteListener(OnCompleteListener<GetTokenResult> { task2 ->
                                if (task2.isSuccessful) {
                                    val idToken = task2.result.token
                                    NetworkManager.initNetworkManager(idToken.toString(), user.uid)

                                    val intent = Intent(
                                        applicationContext,
                                        MapOverviewActivity::class.java
                                    )
                                    startActivity(intent)
                                } else {
                                    Snackbar.make(findViewById(R.id.main), "Token 값이 유효하지 않습니다.", Snackbar.LENGTH_SHORT).show();
                                }
                            })
                    } else {
                        Snackbar.make(findViewById(R.id.main), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }

            override fun onFailure(call: Call<AccountCreateResponse>, err: Throwable) {
                Log.e("Network Error", "Authentication Network Failed. $err")
                Snackbar.make(findViewById(R.id.main), "Authentication Network Failed.", Snackbar.LENGTH_SHORT).show();
            }
        })
    }
}