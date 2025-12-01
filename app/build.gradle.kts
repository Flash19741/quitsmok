plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.quitsmok"
    compileSdk =34

    defaultConfig {
        applicationId = "com.example.quitsmok"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Включаем Data Binding, который необходим для вашего MainActivity.kt
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    // --- Основные зависимости для традиционного подхода ---
    implementation("androidx.core:core-ktx:1.10.1") // Версия взята из контекста
    implementation("androidx.appcompat:appcompat:1.6.1") // Добавлено для AppCompatActivity
    implementation("androidx.fragment:fragment-ktx:1.6.2") // Добавлено для работы с фрагментами

    // --- Зависимости для UI ---
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- Тестовые зависимости (оставлены только необходимые) ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // --------------------------------------------------------------------
    // Все зависимости Jetpack Compose были УДАЛЕНЫ, так как они не нужны
    // и вызывают конфликт с Data Binding.
    // --------------------------------------------------------------------
}
