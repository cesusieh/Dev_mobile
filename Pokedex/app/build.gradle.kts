plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Adicione o plugin 'kotlin-kapt' aqui se você for usar Hilt ou Room com anotações no futuro
    // id("kotlin-kapt")
}

android {
    namespace = "com.example.pokedex"
    compileSdk = 34 // Mude para 34, pois geralmente 36 ainda é uma versão de visualização ou muito recente para compatibilidade completa.
    // Se você estiver usando uma versão específica do Compose Compiler que requer 36, mantenha-o.
    // Caso contrário, 34 é mais seguro para a maioria dos projetos atuais.
    defaultConfig {
        applicationId = "com.example.pokedex"
        minSdk = 24
        targetSdk = 34 // Mude para 34 para corresponder ao compileSdk.
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8 // Mude para 1.8 para compatibilidade com Retrofit/JVM mais comum
        targetCompatibility = JavaVersion.VERSION_1_8 // Mude para 1.8
    }
    kotlinOptions {
        jvmTarget = "1.8" // Mude para 1.8 para compatibilidade
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // Verifique a versão correta do seu compilador Compose
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // NOVAS DEPENDÊNCIAS
    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Retrofit & OkHttp
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(platform(libs.okhttp.bom)) // Adicione a BOM do OkHttp
    implementation(libs.okhttp.logging.interceptor)

    // Coil para carregar imagens
    implementation(libs.coil.compose)

    // ViewModel Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}