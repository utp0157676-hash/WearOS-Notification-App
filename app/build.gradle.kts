plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.menuapp.presentation"
    compileSdk = 36 // <--- Cambiado a 36

    defaultConfig {
        applicationId = "com.example.menuapp.presentation"
        minSdk = 30
        targetSdk = 36 // <--- Cambiado a 36
        versionCode = 1
        versionName = "1.0"
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
    useLibrary("wear-sdk")
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Wear OS Compose Core
    implementation("androidx.wear.compose:compose-material:1.3.0")
    implementation("androidx.wear.compose:compose-foundation:1.3.0")

    // Íconos extendidos
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // Componentes de Compose y Android
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.core.splashscreen)
    implementation(libs.play.services.wearable)

    // Notificaciones y compatibilidad
    implementation("androidx.core:core-ktx:1.12.0")

    debugImplementation(libs.ui.tooling)
}