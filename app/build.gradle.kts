plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.eranbe.setgame"
    compileSdk = 35


    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.eranbe.setgame"
        minSdk = 34
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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // הגדר את ה-API Key עבור בניית Release
            buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyD4at5ipDRMHF0ZLwbw8Q_S1jG4TlWIkZs\"")
        }
        debug {
            // הגדר את ה-API Key עבור בניית Debug
            buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyD4at5ipDRMHF0ZLwbw8Q_S1jG4TlWIkZs\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_15
        targetCompatibility = JavaVersion.VERSION_15
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}