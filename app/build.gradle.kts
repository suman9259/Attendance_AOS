
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("com.google.protobuf")
    id("kotlin-parcelize")
}

android {
    namespace = "com.scharfesicht.attendencesystem"
    compileSdk {
        version = release(36)
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = false
        baseline = file("lint-baseline.xml")
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }

    defaultConfig {
        applicationId = "com.scharfesicht.attendencesystem"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        //noinspection WrongGradleMethod
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }
    }

    signingConfigs {
        create("development") {
            keyAlias = "KeyDev"
            keyPassword = "AttendanceSystemHaiPass"
            storeFile = file("debug.keystore")
            storePassword = "AttendanceSystemHaiPass"
        }
        create("production") {
            keyAlias = "KeyProd"
            keyPassword = "ProdPass"
            storeFile = file("debug.keystore")
//            storeFile = file("/Users/punlearn/AndroidStudioProjects/Attendance System/release.keystore") // Paste Exact path release keystore
            storePassword = "ProdPass"
        }
        /* create("production") {
       keyAlias = System.getenv("RELEASE_KEY_ALIAS")
           ?: localProperties.getProperty("RELEASE_KEY_ALIAS")
       keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
           ?: localProperties.getProperty("RELEASE_KEY_PASSWORD")
       storeFile = file(
           System.getenv("RELEASE_KEYSTORE_PATH")
               ?: localProperties.getProperty("RELEASE_KEYSTORE_PATH")
               ?: "release.keystore"
       )
       storePassword = System.getenv("RELEASE_STORE_PASSWORD")
           ?: localProperties.getProperty("RELEASE_STORE_PASSWORD")
   }*/
    }

    flavorDimensions += listOf("environment")

    productFlavors {
        create("development") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"

            buildConfigField("String", "BASE_URL", "\"https://hrmpro.time-365.com\"")
            buildConfigField("String", "COMPANY_CODE", "\"188264\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "false")
            buildConfigField("boolean", "DEBUG_MODE", "true")

            resValue("string", "app_name", "Attendence Dev")
        }

        create("production") {
            dimension = "environment"

            buildConfigField("String", "BASE_URL", "\"https://hrmpro.time-365.com\"")
            buildConfigField("String", "COMPANY_CODE", "\"188264\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "true")
            buildConfigField("boolean", "DEBUG_MODE", "false")

            resValue("string", "app_name", "Attendence")
        }
    }

    buildTypes {
        debug {
//            signingConfig = signingConfigs.getByName("development")
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
        }

        release {
            signingConfig = signingConfigs.getByName("production")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            packaging {
                resources {
                    excludes += listOf(
                        "META-INF/DEPENDENCIES",
                        "META-INF/LICENSE",
                        "META-INF/LICENSE.txt",
                        "META-INF/license.txt",
                        "META-INF/NOTICE",
                        "META-INF/NOTICE.txt",
                        "META-INF/notice.txt",
                        "META-INF/ASL2.0",
                        "META-INF/*.kotlin_module",
                        "kotlin/**",
                        "**.properties",
                        "**.bin"
                    )
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = false
        dataBinding = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

// Custom APK naming
androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val appName = "Attendence System"
            val flavorName = variant.flavorName ?: "default"
            val buildType = variant.buildType ?: "debug"
            val versionName = variant.outputs.first().versionName.orNull ?: "1.0.0"
            val versionCode = variant.outputs.first().versionCode.orNull ?: 1

            output.versionName.set(
                "${appName}-${flavorName}-${buildType}-v${versionName}(${versionCode}).apk"
            )
        }
    }
}

dependencies {

    val absherSdkVersion = project.findProperty("ABSHER_SDK_VERSION") as String? ?: "1.0.2"
    implementation("sa.gov.moi:interior-sdk:$absherSdkVersion")

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.profileinstaller)

    // Camera X
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Lifecycle & Navigation
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Coil
    implementation(libs.coil.compose)

    //Location Service
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Accompanist
    implementation(libs.accompanist.placeholder.material3)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.accompanist.navigation.animation)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)

    // AWS Image Recognization
    implementation("com.amazonaws:aws-android-sdk-core:2.73.0")
    implementation("com.amazonaws:aws-android-sdk-rekognition:2.73.0")
    implementation("com.amazonaws:aws-android-sdk-cognitoidentityprovider:2.73.0")

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Gson
    implementation(libs.gson)

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.core.okio)

    // Protobuf
    implementation(libs.protobuf.javalite)

    // AndroidX extras
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.material)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

