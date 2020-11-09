plugins {
    `maven-publish`
    kotlin("multiplatform") version "1.4.0-rc"
}

group = "org.jetbrains.mppna"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    
    sourceSets {
        val commonMain by getting
        val jvmMain by getting {
            dependencies {
                api("net.java.dev.jna:jna:5.5.0")
            }
        }
        val nativeMain by getting
    }
}