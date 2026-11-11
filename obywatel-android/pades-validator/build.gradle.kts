// SPDX-FileCopyrightText: 2026 mObywatel-2.0 contributors
// SPDX-License-Identifier: EUPL-1.2
plugins {
    id("java-library")
}

group = "pl.gov.obywatel"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    api("eu.europa.ec.joinup.sd-dss:dss-validation:6.5.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-pades:6.5.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-service:6.5.1")
    implementation("eu.europa.ec.joinup.sd-dss:dss-tsl-validation:6.5.1")
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Test>().configureEach {
    useJUnit()
}
