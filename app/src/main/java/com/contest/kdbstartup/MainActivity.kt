package com.contest.kdbstartup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.contest.kdbstartup.utils.DoubleBackPressHandler
import com.contest.kdbstartup.utils.hideDarkOverlay
import com.contest.kdbstartup.utils.showDarkOverlay
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kakao.vectormap.KakaoMapSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    //뒤로가기 기능
    private val doubleBackPressHandler = DoubleBackPressHandler(this)
    
    private var mAuth: FirebaseAuth? = null

    //android.os.TransactionTooLargeException 발생 시 CredentialManager가 작동하지 않아 구버전을 혼합하여 사용
    //계정이 많은 경우 발생으로 추정 중
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    
    //신버전 먼저 시도 후 안되면 구버전으로 실행시킬 용도
    private var oldGoogleLoginjob: Job? = null


    public override fun onCreate(savedInstanceState: Bundle?) {
        KakaoMapSdk.init(this@MainActivity, "df17ea99e1579611972ffbb1ff069e51");
        KakaoSdk.init(this@MainActivity, "df17ea99e1579611972ffbb1ff069e51");

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAuth = FirebaseAuth.getInstance()

        findViewById<SignInButton>(R.id.google_sign_btn).setOnClickListener {
            this.showDarkOverlay()
            signIn() //CredentialManager 사용 로그인
            this.hideDarkOverlay()
        }

        initGoogle()
        initKakao()
        doubleBackPressHandler.enable()
    }

    private fun initKakao(){
        val kakaoSignInButton = findViewById<Button>(R.id.kakao_sign_btn)
        kakaoSignInButton.setOnClickListener {
            this.showDarkOverlay()

            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    this.hideDarkOverlay()
                    Snackbar.make(findViewById(R.id.main), "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG).show()
                } else if (token != null) {
                    //아래처럼 조작이 가능해진다는 문제점을 해결
                    //mAuth!!.createUserWithEmailAndPassword("fake@fake.com", "fakepassword")

                    retrieveCustomToken("kakao", token.accessToken)
                }
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@MainActivity)) {
                UserApiClient.instance.loginWithKakaoTalk(this@MainActivity) { token, error ->
                    if (error != null) {
                        this.hideDarkOverlay()
                        Log.e("Kakao Login", "카카오톡으로 로그인 실패", error)

                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            Snackbar.make(findViewById(R.id.main), "로그인을 취소하였습니다.", Snackbar.LENGTH_LONG).show()
                            return@loginWithKakaoTalk
                        }

                        UserApiClient.instance.loginWithKakaoAccount(this@MainActivity, callback = callback)
                    } else if (token != null) {
                        retrieveCustomToken("kakao", token.accessToken)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this@MainActivity, callback = callback)
            }
        }
    }
    
    @Deprecated("구버전 API 사용 중, 이후 CredentialManager 관련 문제 해결 시 삭제")
    private fun initGoogle(){
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent: Intent? = result.data
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    // 구글 로그인은 성공, Firebase에 로그인
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

                    retrieveCustomToken("google", account.idToken!!)
                } catch (e: ApiException) {
                    // 구글 로그인 실패
                    Log.e("Google Login Error", e.toString())
                    Snackbar.make(findViewById(R.id.main), "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_sdk_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.google_sdk_id))
            .setAutoSelectEnabled(true)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(this@MainActivity)

        oldGoogleLoginjob = lifecycleScope.launch {
            delay(3000)
            //3초를 기다려보고 신버전 로그인 창이 뜨지 않으면 구버전 로그인 진행
            val signInIntent = mGoogleSignInClient!!.signInIntent
            activityResultLauncher!!.launch(signInIntent)
        }

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@MainActivity,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e("Google Credential Error", e.toString())
                Snackbar.make(findViewById(R.id.main), "인증 절차에 실패했습니다.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
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
                        Snackbar.make(findViewById(R.id.main), "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG).show()
                        Log.e("Google Login Error", "Received an invalid google id token response", e)
                    }
                } else {
                    Snackbar.make(findViewById(R.id.main), "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG).show()
                    Log.e("Google Login Error", "Unexpected type of credential")
                }
            }
            else -> {
                Snackbar.make(findViewById(R.id.main), "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG).show()
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

    public override fun onPause() {
        super.onPause()
        //구글 구버전 로그인을 위해 사용
        //만약 포커스를 나가면 로그인 액티비티로 이동한 것으로 판단
        oldGoogleLoginjob?.cancel()
    }

    public override fun onDestroy() {
        super.onDestroy()
        doubleBackPressHandler.disable()
    }

    private fun isUserNonNull(): Boolean {
        return mAuth!!.currentUser != null
    }

    private fun updateUI() {
        //로그인이 이미 되어있으므로 TODO
        val intent = Intent(
            this@MainActivity,
            MapOverviewActivity::class.java
        )
        startActivity(intent)
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
                            .addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    val idToken = task2.result.token
                                    NetworkManager.initNetworkManager(idToken.toString(), user.uid)

                                    val intent = Intent(
                                        this@MainActivity,
                                        MapOverviewActivity::class.java
                                    )
                                    startActivity(intent)
                                } else {
                                    Snackbar.make(
                                        findViewById(R.id.main),
                                        "Token 값이 유효하지 않습니다.",
                                        Snackbar.LENGTH_SHORT
                                    ).show();
                                }
                            }
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