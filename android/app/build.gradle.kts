plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.fettqa.events.android"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.fettqa.events.android"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Emulator: use with `adb reverse tcp:8080 tcp:8080` (reliable on Windows).
        // Alternative without reverse: http://10.0.2.2:8080/
        // Physical device: http://<your-PC-LAN-IP>:8080/
        buildConfigField("String", "BASE_URL", "\"http://127.0.0.1:8080/\"")
        //buildConfigField("String", "BASE_URL", "\"https://event-registration-jesq.onrender.com/\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
