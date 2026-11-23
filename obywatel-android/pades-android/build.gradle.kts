// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
plugins {
    id("com.android.library")
}

group = "pl.gov.obywatel"
version = "0.1.0"

android {
    namespace = "pl.gov.obywatel.pades"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Keep DSS pinned to the same release as the JVM validator.
    api("eu.europa.ec.joinup.sd-dss:dss-validation:6.5.1")
    api("eu.europa.ec.joinup.sd-dss:dss-pades:6.5.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-pades-pdfbox:6.5.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-cms-object:6.5.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-service:6.5.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-tsl-validation:6.5.1")
}
