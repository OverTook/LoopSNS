plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.android.kapt)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.hci.loopsns"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hci.loopsns"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
//    kotlinOptions {
//        jvmTarget = "21"
//    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.flexbox)

    implementation(libs.play.services.maps) //지도 라이브러리

    implementation(libs.v2.all)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore.ktx)
    implementation(platform(libs.firebase.bom)) //파이어베이스 인증 라이브러리
    implementation(libs.firebase.auth.ktx)

    implementation(libs.firebase.messaging) // 파이어베이스 FCM 메세징 라이브러리

    implementation(libs.play.services.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.location)
    implementation(libs.googleid)

    implementation(libs.circleimageview) //원형 이미지 라이브러리
    implementation(libs.glide) //이미지 로딩 라이브러리

    implementation(libs.androidx.credentials) //구글 인증 라이브러리
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation (libs.material.v121)
    implementation (libs.androidx.viewpager2)



    implementation (libs.lottie) //로티 애니메이션 라이브러리

    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.work.runtime.ktx) //새로고침 레이아웃 라이브러리

    //implementation("top.zibin:Luban:1.1.8") //이미지 압축 라이브러리
    implementation("io.github.lucksiege:compress:v3.11.2") //자체 이미지 압축 라이브러리
    implementation("io.github.lucksiege:ucrop:v3.11.2") //이미지 크롭
    //implementation (libs.pictureselector) //갤러리 라이브러리, Android 14 권한 문제로 제거

    implementation("com.github.guolindev:litepal:master-SNAPSHOT") //SQLite 사용 라이브러리

    implementation("androidx.core:core-splashscreen:1.0.0") //스플래시 스크린
    implementation("com.github.skydoves:androidveil:1.1.4") //로딩하는 동안 데이터 가리기

    implementation("com.github.st235:expandablebottombar:1.5.3") //하단 메뉴 디자인 라이브러리

    implementation("com.afollestad.material-dialogs:input:3.3.0") //팝업
    //implementation("com.github.YarikSOffice:lingver:1.3.0") //다국어 처리

    implementation("com.saadahmedev.popup-dialog:popup-dialog:2.0.0") //다이얼로그
    //implementation("com.royrodriguez:transitionbutton:0.2.0") //로그인 UI 디자인용

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(fileTree(mapOf(
        "dir" to "libs",
        "include" to listOf("*.aar")
        //"include" to listOf("*.aar", "*.jar")
    ))) //갤러리 라이브러리 버그로 인하여 추가

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}