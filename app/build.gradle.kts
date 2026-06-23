plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.fieldlog.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fieldlog.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "3.1"
    }

    // Sign debug builds with a fixed keystore committed to the repo so every CI
    // build shares one signature. Without this, GitHub's runners generate a new
    // random debug key each build, and Android then refuses to install an update
    // over a previous build ("package conflicts with an existing package"). This
    // key is debug-only and carries no security value, so committing it is safe.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.9.0")
}
