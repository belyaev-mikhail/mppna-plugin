import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    `java-gradle-plugin`
    id("org.gradle.kotlin.kotlin-dsl") version "1.4.2"
    kotlin("jvm") version "1.4.0-rc"
}

group = "org.jetbrains.mppna"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("mppnaGradlePlugin") {
            id = "org.jetbrains.mppna-gradle-plugin"
            implementationClass = "mppna.gradle.plugin.MppnaPlugin"
        }
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    api("com.nativelibs4java:jnaerator:0.12")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
