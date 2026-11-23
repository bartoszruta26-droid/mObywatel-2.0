// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Monotoniczny numer kompilacji wspólny dla lokalnych i CI buildów.
 // Pozwala instalatorowi Androida rozpoznać każdą kolejną kompilację jako aktualizację.
val buildEpochSeconds = System.currentTimeMillis() / 1000L
val monotonicVersionCode = (buildEpochSeconds - 700_000_000L).toInt()

// Stable upload signing is supplied only through environment variables / CI secrets.
// The keystore itself is never stored in the repository.
val signingStorePath = System.getenv("ANDROID_KEYSTORE_FILE")
val signingStorePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val signingKeyAlias = System.getenv("ANDROID_KEY_ALIAS")
val signingKeyPassword = System.getenv("ANDROID_KEY_PASSWORD")
val hasStableSigning = listOf(
    signingStorePath, signingStorePassword, signingKeyAlias, signingKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "pl.gov.obywatel.android"
    compileSdk = 37

    defaultConfig {
        applicationId = "pl.gov.obywatel.android.prototype"
        minSdk = 26
        targetSdk = 35
        versionCode = monotonicVersionCode
        versionName = "0.3.$buildEpochSeconds"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasStableSigning) {
            create("stableUpload") {
                storeFile = file(signingStorePath!!)
                storeType = "PKCS12"
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            if (hasStableSigning) signingConfig = signingConfigs.getByName("stableUpload")
        }
        getByName("release") {
            if (hasStableSigning) signingConfig = signingConfigs.getByName("stableUpload")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    // DSS/PDFBox dependencies contain duplicate informational Maven metadata.
    // DEPENDENCIES is not runtime code; exclude it from the APK merge.
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            // Preserve and concatenate third-party license texts instead of dropping them.
            merges += "META-INF/LICENSE.md"
            // Preserve the distinct upstream attribution notices in the packaged APK.
            merges += "META-INF/NOTICE.md"
            // JAXB build-time binding descriptors are not needed by the Android runtime.
            // Exclude duplicate metadata rather than concatenate invalid XML documents.
            excludes += "xsd/bindings.xml"
            // JAXB episode descriptors are build-time metadata and are not needed at runtime.
            excludes += "META-INF/sun-jaxb.episode"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(project(":pades-android"))
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.1")
    implementation("info.guardianproject:tor-android:0.4.9.14")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.caverock:androidsvg-aar:1.4")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.mlkit:translate:17.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
