import java.net.URI

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
    dependencies {}
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
        set("kotlinVersion", "1.3.50")
    }
}
