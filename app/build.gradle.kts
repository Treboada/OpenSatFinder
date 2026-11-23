import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
}

/** SDK levels live in the version catalog; this keeps the access readable. */
fun sdkLevel(provider: Provider<String>): Int = provider.get().toInt()

/** Same for a component version that is used as a string. */
fun sdkVersion(provider: Provider<String>): String = provider.get()

val compileSdkLevel = sdkLevel(libs.versions.android.compileSdk)
val buildToolsRevision = sdkVersion(libs.versions.android.buildTools)
val minSdkLevel = sdkLevel(libs.versions.android.minSdk)
val targetSdkLevel = sdkLevel(libs.versions.android.targetSdk)

android {
    namespace = "net.treboada.opensatfinder"
    compileSdk = compileSdkLevel
    buildToolsVersion = buildToolsRevision

    defaultConfig {
        applicationId = "net.treboada.opensatfinder"
        minSdk = minSdkLevel
        targetSdk = targetSdkLevel
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ktlint {
    android.set(true)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
