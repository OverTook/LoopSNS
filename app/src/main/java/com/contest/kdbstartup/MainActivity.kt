package com.contest.kdbstartup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.vectormap.KakaoMapSdk


class MainActivity : AppCompatActivity() {

    private lateinit var signInButton: SignInButton
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        KakaoMapSdk.init(this, "df17ea99e1579611972ffbb1ff069e51");
        Log.e("KeyHash", Utility.getKeyHash(this))

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        init()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signInButton = findViewById<SignInButton>(R.id.google_sign_btn)
        signInButton.setOnClickListener(View.OnClickListener { signIn() })
    }

    private fun init() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback<ActivityResult> { result ->
                if(result.resultCode == RESULT_OK) {
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
                        Snackbar.make(findViewById(R.id.main), "구글 로그인에 실패했습니다.", Snackbar.LENGTH_SHORT).show();
                    }
                }

            })
        configSignIn()
        initAuth()

        Toast.makeText(applicationContext, "Init Complete", Toast.LENGTH_LONG).show()
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

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    val user = mAuth!!.currentUser
                    //Snackbar.make(findViewById(R.id.main), "Complete " + user!!.uid, Snackbar.LENGTH_SHORT).show();

                    //토큰을 통해 NetworkManager 체인 연결
                    user!!.getIdToken(true)
                        .addOnCompleteListener(OnCompleteListener<GetTokenResult> { task2 ->
                            if (task2.isSuccessful) {
                                val idToken = task2.result.token
                                Toast.makeText(applicationContext, "Complete " + idToken.toString(), Toast.LENGTH_SHORT).show();
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