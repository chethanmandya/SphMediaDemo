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
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "APPLICATION_BASE_URL", "https://api.openbrewerydb.org/")

    }




    buildTypes {
        // 'release' build type configuration
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "retrofit2.pro",
                "gson.pro",
                "okhttp3.pro",
                "firebase-crashlytics.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "APPLICATION_BASE_URL", "\"https://api.openbrewerydb.org/\"")
        }

        // 'debug' build type configuration
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            enableUnitTestCoverage = true
            buildConfigField("String", "APPLICATION_BASE_URL", "\"https://api.openbrewerydb.org/\"")
        }

        // 'stg' (staging) build type configuration
        create("stg") {
            isMinifyEnabled = false
            isShrinkResources = false
            buildConfigField("String", "APPLICATION_BASE_URL", "\"https://api.openbrewerydb.org/\"")
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs.toMutableList().apply {
            add("-opt-in=kotlin.RequiresOptIn")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":common"))

    // Core
    implementation(libs.androidx.core)

    // Activity
    implementation(libs.androidx.activity.compose)

    // Compose ui
    implementation(libs.androidx.compose.ui.ui)
    implementation(libs.androidx.compose.ui.ui.tooling.preview)


    // Work manager
    implementation(libs.androidx.work)

    // Coroutine
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Retrofit
    implementation(libs.bundles.retrofit2)

    // Okhttp
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.paging.common.android)
    implementation(libs.androidx.paging.compose.android)
    testImplementation(libs.androidx.test.junit)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)


    androidTestImplementation("androidx.room:room-testing:2.5.2")


    // Hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.google.dagger.hilt.android)
    ksp(libs.google.dagger.hilt.android.compiler)

    // Navigation
    implementation(libs.androidx.navigation)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // LiveData
    implementation(libs.androidx.livedata)

    // Gson
    implementation(libs.google.gson)

    // Timber
    implementation(libs.timber)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // fonts
    implementation(libs.androidx.ui.text.google.fonts)

    implementation(libs.accompanist.flowlayout.v0300)

    // Constrain layout
    implementation(libs.androidx.constraintlayout.compose) // Add this line

    implementation(libs.accompanist.systemuicontroller.v0280)


    implementation(libs.material.v190)







    testImplementation("app.cash.turbine:turbine:0.12.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("androidx.test:runner:1.5.2")
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
}

