
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.sph.sphmedia"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.sph.sphmedia"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "APPLICATION_BASE_URL", "https://api.openbrewerydb.org/")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "retrofit2.pro",
                "gson.pro",
                "okhttp3.pro",
                "firebase-crashlytics.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "APPLICATION_BASE_URL", "\"https://api.openbrewerydb.org/\"")
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            enableUnitTestCoverage = true
            buildConfigField("String", "APPLICATION_BASE_URL", "\"https://api.openbrewerydb.org/\"")
        }

        create("stg") {
            isMinifyEnabled = false
            isShrinkResources = false
            buildConfigField("String", "APPLICATION_BASE_URL", "\"https://api.openbrewerydb.org/\"")
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // Project modules
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":common"))

    // Core dependencies
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)

    // Compose and UI components
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.google.accompanist.systemUiController)
    implementation(libs.accompanist.flowlayout.v0300)

    // AndroidX & WorkManager
    implementation(libs.androidx.work)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.material3.android)

    // Coroutine libraries
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Networking (Retrofit and OkHttp)
    implementation(libs.bundles.retrofit2)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.retrofit2)
    implementation(libs.converter.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
    androidTestImplementation(libs.androidx.room.testing)

    // Hilt and Dagger
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.google.dagger.hilt.android)
    ksp(libs.google.dagger.hilt.android.compiler)

    // Lifecycle and ViewModel
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.livedata)

    // Navigation
    implementation(libs.androidx.navigation)

    // Utility libraries
    implementation(libs.google.gson)
    implementation(libs.timber)
    implementation(libs.androidx.ui.text.google.fonts)

    // Material and UI libraries
    implementation(libs.material.v190)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.runner)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit.ktx)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4.v174)
    debugImplementation(libs.androidx.ui.tooling.v174)
    debugImplementation(libs.androidx.ui.test.manifest.v154)

    // MockWebServer for testing
    testImplementation(libs.mockwebserver)

    // Paging
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.common.ktx)

    // Mockito for testing
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.mockito.kotlin)
}