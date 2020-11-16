import mppna.gradle.plugin.MppnaPlugin

plugins {
    kotlin("multiplatform") version "1.4.0-rc"
}

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath("org.jetbrains.mppna:mppna-plugin:1.0.0")
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
    }
    mavenLocal()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    sourceSets {
        val commonMain by getting
        val nativeMain by getting
        val jvmMain by getting
    }
}

apply<MppnaPlugin>()

val run by tasks.creating(JavaExec::class) {
    main = "MainKt"
    kotlin {
        val main = targets["jvm"].compilations["main"]
        dependsOn(main.compileAllTaskName)
        classpath(
            { main.output.allOutputs.files },
            { configurations["jvmRuntimeClasspath"] }
        )
    }
}
