plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:calendar:domain"))
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            // Multiplatform ViewModel + viewModelScope. NO Compose here — UI stays swappable.
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.koin.core)
            // Koin's multiplatform ViewModel DSL (viewModelOf) lives in its own artifact.
            implementation(libs.koin.core.viewmodel)
        }
    }
}

android {
    namespace = "com.vacation.feature.calendar.presentation"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
