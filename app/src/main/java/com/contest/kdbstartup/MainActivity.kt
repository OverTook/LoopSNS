package com.contest.kdbstartup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.contest.kdbstartup.network.AccountCreateResponse
import com.contest.kdbstartup.network.NetworkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.kakao.vectormap.KakaoMapSdk
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInButton: SignInButton
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        KakaoMapSdk.init(applicationContext, "df17ea99e1579611972ffbb1ff069e51");
        KakaoSdk.init(applicationContext, "df17ea99e1579611972ffbb1ff069e51");
        //Log.e("KeyHash", Utility.getKeyHash(this))

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        init()

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

    private fun init() {
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

                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    // 구글 로그인 실패
                    Log.e("Error", e.toString())
                    Snackbar.make(findViewById(R.id.main), "구글 로그인에 실패했습니다.", Snackbar.LENGTH_SHORT)
                        .show();
                }
            }

        }
        configSignIn()
        initAuth()
    }

    private fun configSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_sdk_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initAuth() {
        mAuth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        if (isUserNonNull()) {
            updateUI()
        }
    }

    private fun isUserNonNull(): Boolean {
        return if (mAuth!!.currentUser == null) {
            false
        } else {
            true
        }
    }

    private fun updateUI() {
        //로그인이 이미 되어있으므로 TODO
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        activityResultLauncher!!.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        if (acct.idToken == null) {
            Snackbar.make(findViewById(R.id.main), "ID 토큰을 얻어올 수 없습니다.", Snackbar.LENGTH_SHORT).show();
            return
        }

        retrieveCustomToken("google", acct.idToken!!)
//        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
//        mAuth!!.signInWithCredential(credential)
//            .addOnCompleteListener(
//                this
//            ) { task ->
//                if (task.isSuccessful) {
//                    val user = mAuth!!.currentUser
//                    //Snackbar.make(findViewById(R.id.main), "Complete " + user!!.uid, Snackbar.LENGTH_SHORT).show();
//
//                    //토큰을 통해 NetworkManager 체인 연결
//                    user!!.getIdToken(true)
//                        .addOnCompleteListener(OnCompleteListener<GetTokenResult> { task2 ->
//                            if (task2.isSuccessful) {
//                                val idToken = task2.result.token
//                                Toast.makeText(applicationContext, "Complete " + idToken.toString(), Toast.LENGTH_SHORT).show();
//                                NetworkManager.initNetworkManager(idToken.toString(), user.uid)
//
//                                val intent = Intent(
//                                    applicationContext,
//                                    MapOverviewActivity::class.java
//                                )
//                                startActivity(intent)
//                            } else {
//                                Snackbar.make(findViewById(R.id.main), "Token 값이 유효하지 않습니다.", Snackbar.LENGTH_SHORT).show();
//                            }
//                        })
//                } else {
//                    Snackbar.make(findViewById(R.id.main), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                }
//            }
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
                        //Snackbar.make(findViewById(R.id.main), "Complete " + user!!.uid, Snackbar.LENGTH_SHORT).show();

                        //토큰을 통해 NetworkManager 체인 연결
                        user!!.getIdToken(true)
                            .addOnCompleteListener(OnCompleteListener<GetTokenResult> { task2 ->
                                if (task2.isSuccessful) {
                                    val idToken = task2.result.token
                                    //Toast.makeText(applicationContext, "Complete " + idToken.toString(), Toast.LENGTH_SHORT).show();
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

    private fun signOut() {
        // Firebase 로그아웃
        mAuth!!.signOut()

        // Google 로그아웃
        mGoogleSignInClient!!.signOut().addOnCompleteListener(
            this
        ) { Toast.makeText(applicationContext, "Complete", Toast.LENGTH_LONG).show() }
    }

    private fun revokeAccess() {
        // Firebase 로그아웃
        mAuth!!.signOut()

        // Google 엑세스 취소
        mGoogleSignInClient!!.revokeAccess().addOnCompleteListener(
            this
        ) { Toast.makeText(applicationContext, "Complete", Toast.LENGTH_LONG).show() }
    }
}