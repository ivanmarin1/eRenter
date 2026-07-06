plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
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
            implementation(libs.koin.core)

            // SQLDelight: generated queries live in commonMain; drivers are per-platform below.
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.driver.sqlite)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.native)
        }
    }
}

sqldelight {
    databases {
        create("BookingDatabase") {
            packageName.set("com.vacation.feature.calendar.data.db")
            // Note: verifyMigrations is intentionally left off. With the schema defined in the
            // .sq files, SQLDelight's verifier replays .sqm migrations from an empty database
            // (which would require the full schema to live in migrations). The migrations here
            // are runtime upgrades applied by the platform drivers (Android/iOS auto, JVM via the
            // schema-aware JdbcSqliteDriver), which is what is exercised on real devices.
        }
    }
}

android {
    namespace = "com.vacation.feature.calendar.data"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}
