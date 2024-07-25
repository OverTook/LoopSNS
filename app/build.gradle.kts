plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
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
        dataBinding = true
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

    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation(libs.kakaomap)
    implementation("com.kakao.sdk:v2-all:2.20.3")
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(platform("com.google.firebase:firebase-bom:32.3.1")) //파이어베이스 인증 라이브러리
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.location)
    implementation(libs.googleid)

    implementation("de.hdodenhof:circleimageview:3.1.0") //원형 이미지 라이브러리
    implementation("com.github.bumptech.glide:glide:4.16.0") //이미지 로딩 라이브러리

    implementation("androidx.credentials:credentials:<latest version>") //구글 인증 라이브러리
    implementation("androidx.credentials:credentials-play-services-auth:<latest version>")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation ("com.google.android.material:material:1.2.1")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")

    implementation ("io.github.lucksiege:pictureselector:v3.11.2") //갤러리 라이브러리

    implementation ("com.airbnb.android:lottie:6.4.1") //로티 애니메이션 라이브러리

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}