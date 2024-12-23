plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.nom_food_kotlin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nom_food_kotlin"
        minSdk = 26
        targetSdk = 34
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

    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //glide
    implementation("com.github.bumptech.glide:glide:4.12.0")

//    // img_picker
//    implementation("com.github.dhaval2404:imagepicker:2.1")
//
//    // googlemap
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation("com.google.maps.android:android-maps-utils:2.2.5")

    implementation("androidx.fragment:fragment-ktx:1.8.2")

    implementation("com.android.volley:volley:1.2.1")

    //coroutines

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // firebase auth
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.code.gson:gson:2.10.1")

    // Toast message
    implementation("com.github.Spikeysanju:MotionToast:1.4")

    // icon animation
    implementation("com.airbnb.android:lottie:3.7.0")

    // Stripe
    implementation("com.stripe:stripe-android:20.31.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
}