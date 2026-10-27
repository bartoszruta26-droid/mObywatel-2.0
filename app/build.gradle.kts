plugins {
    id("com.android.application")
}

android {
    namespace = "com.dokumentownia.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dokumentownia.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-szkielet"
        resourceConfigurations += listOf("pl")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity:1.9.2")
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
}
