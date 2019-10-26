import java.net.URI

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
        classpath("org.jetbrains.kotlin:kotlin-android-extensions:1.3.50")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven { url = URI.create("https://jitpack.io") }
    }
    ext {
        set("bc_version", "1.61")
        set("kotlin_version", "1.3.50")
    }
}
