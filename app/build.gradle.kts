plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.itunesapi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.itunesapi"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // 🔐 유튜브 API 키 (local.properties)
        val youtubeKey = if (project.hasProperty("YOUTUBE_API_KEY")) {
            project.property("YOUTUBE_API_KEY") as String
        } else {
            ""
        }
        buildConfigField("String", "YOUTUBE_API_KEY", "\"$youtubeKey\"")

        // 🌦️ OWM (OpenWeatherMap) API 키
        val owmKey = project.rootProject.file("local.properties")
            .readLines()
            .find { it.startsWith("OWM_API_KEY=") }
            ?.substringAfter("=")
            ?: ""
        buildConfigField("String", "OWM_API_KEY", "\"$owmKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // AndroidX + UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    // Firebase
    implementation("com.google.firebase:firebase-auth-ktx:22.1.1")
    implementation("com.google.firebase:firebase-firestore-ktx:24.9.1")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Google Play Services
    implementation(libs.play.services.location)

    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
