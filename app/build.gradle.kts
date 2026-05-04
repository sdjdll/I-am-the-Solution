plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "sdjini.solution"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "sdjini.solution"
        minSdk = 26
        targetSdk = 36
        versionCode = 13
        versionName = "1.1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    packaging{
        resources{
            excludes += setOf("META-INF/**")
            excludes += setOf("DebugProbesKt.bin")
            excludes += setOf("kotlin/**")
            excludes += setOf("res/**")
        }
    }
}

dependencies {
//    implementation(libs.appcompat)
//    implementation(libs.material)
    implementation(libs.activity)
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("androidx.documentfile:documentfile:1.1.0")
//    implementation(libs.constraintlayout)
//    implementation(libs.recyclerview)
//    implementation(libs.cardview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}