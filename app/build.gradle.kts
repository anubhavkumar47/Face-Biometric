plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.facebiometric.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.facebiometric.app"
        minSdk = 30
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
    buildFeatures {
        viewBinding = true
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
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.litert)
    implementation(libs.litert.support.api)

    //firebase
    implementation(libs.firebase.database)
    implementation ("com.google.firebase:firebase-storage:20.2.1")

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Google Play Services (Latest Version)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // UI Libraries
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.lzyzsd:circleprogress:1.2.4") // Circular progress bar

    // CameraX Core
    implementation ("androidx.camera:camera-core:1.3.0")

    // CameraX Lifecycle
    implementation ("androidx.camera:camera-lifecycle:1.3.0")

    // CameraX View for Preview
    implementation ("androidx.camera:camera-view:1.3.0")

    // CameraX Camera2 (Required!)
    implementation ("androidx.camera:camera-camera2:1.3.0")

    // ML Kit Face Detection
    implementation ("com.google.mlkit:face-detection:16.1.5")

    // API Services
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")




}

