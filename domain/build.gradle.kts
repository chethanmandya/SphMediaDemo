plugins {
    // Android library plugin for shared libraries, not standalone apps
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") // Kotlin Symbol Processing (KSP)
}

android {
    namespace = "ccom.sphmedia.domain"
    // Use version constants from 'libs.versions.toml' file for SDK versions
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "com.sph.sphmedia.app.CustomTestRunner"
    }

    buildTypes {
        // 'release' build type configuration
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        // 'debug' build type configuration
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            enableUnitTestCoverage = true
        }

        // 'stg' (staging) build type configuration
        create("stg") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":common"))
    implementation(project(":data"))



    implementation(libs.androidx.lifecycle.livedata.ktx)

    //paging
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.common.ktx)


    // Hilt
    implementation(libs.google.dagger.hilt.android)
    ksp(libs.google.dagger.hilt.android.compiler)

    // Timber
    implementation(libs.timber)

    // Retrofit
    implementation(libs.retrofit2)

    // OkHttp
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging)

    // LiveData
    implementation(libs.androidx.compose.runtime.livedata)

    // Room
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)



}