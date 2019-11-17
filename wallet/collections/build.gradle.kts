import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val kotlinVersion = "1.3.50"
    val ktLintVersion = "9.1.1"
    repositories {
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:$ktLintVersion")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.50")
    id("org.jlleitschuh.gradle.ktlint").version("9.1.1")
    id("java-library")
}

group = "org.hermes"
version = "1.0.0"

repositories {
    mavenCentral()
}

sourceSets["main"].java.srcDir("src/main/kotlin")
sourceSets["test"].java.srcDir("src/test/kotlin")

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
