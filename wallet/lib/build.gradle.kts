plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(29)
    }
    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("src/test/kotlin")
}

dependencies {
    implementation("com.github.iotaledger:iota-java:1.0.0-beta5")
    implementation("org.bouncycastle:bcpkix-jdk15on:${project.ext.get("bc_version") as String}")
    implementation("org.bouncycastle:bcprov-jdk15on:${project.ext["bc_version"]}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${project.ext["kotlin_version"]}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${project.ext["kotlin_version"]}")
    implementation("org.web3j:core:4.2.0-android")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("androidx.test.espresso:espresso-core:3.2.0")
    testImplementation("androidx.test.ext:junit:1.1.1")
    testImplementation("androidx.test:runner:1.2.0")
    testImplementation("it.ozimov:assertj:2.0.0")
    testImplementation("org.bitcoinj:bitcoinj-core:0.15.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.5.1")
}

//tasks.named<Test>("test") {
//    useJUnitPlatform()
//}
