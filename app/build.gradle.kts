plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
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

    implementation (libs.pictureselector) //갤러리 라이브러리

    implementation (libs.lottie) //로티 애니메이션 라이브러리

    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.work.runtime.ktx) //새로고침 레이아웃 라이브러리

    implementation("id.zelory:compressor:3.0.1") //이미지 압축 라이브러리

    implementation("org.litepal.guolindev:core:3.2.3") //SQLite 사용 라이브러리

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
