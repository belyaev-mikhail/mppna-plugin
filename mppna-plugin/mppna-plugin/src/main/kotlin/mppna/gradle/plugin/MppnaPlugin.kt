package mppna.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.io.File
import java.util.*

class MppnaPlugin : Plugin<Project> {
    lateinit var project: Project
    lateinit var mppExtension: KotlinMultiplatformExtension
    val cinteropSettingsList = mutableListOf<CinteropSettings>()

    class CinteropSettings(defFile: File) {
        val linkerOpts: List<String>
        val compilerOpts: List<String>
        val jnaLibraryPaths: List<String>
        val library: String
        val headers: List<String>

        init {
            val props = Properties()
            props.load(defFile.inputStream())

            linkerOpts = (props["linkerOpts"] as? String)?.split("\\s".toRegex()).orEmpty()
            compilerOpts = (props["compilerOpts"] as? String)?.split("\\s".toRegex()).orEmpty()
            jnaLibraryPaths = linkerOpts.filter { opt -> opt.startsWith("-L") }.map { opt -> opt.removePrefix("-L") }
            library = defFile.name.removePrefix("lib").removeSuffix(".def")

            val includePath = compilerOpts.first { it.startsWith("-I") }.removePrefix("-I")
            headers = (props["headers"] as? String)?.split("\\s".toRegex()).orEmpty().map { "$includePath/$it" }
        }
    }

    private fun processDefFiles(withTests: Boolean = true) {
        val defFiles: List<File> = project.projectDir.resolve("src/nativeInterop/cinterop").listFiles()
                ?.filter { file -> file.name.endsWith(".def") }.orEmpty()
        cinteropSettingsList.addAll(defFiles.map { CinteropSettings(it) })
        val jnaLibraryPath = cinteropSettingsList.map { defFile -> defFile.jnaLibraryPaths }.flatten().distinct().joinToString(":")

        // jvm
        project.tasks.withType(JavaExec::class) {
            systemProperty("jna.library.path", jnaLibraryPath)
        }
        if (withTests) {
            project.tasks.withType(Test::class) {
                systemProperty("jna.library.path", jnaLibraryPath)
            }
        }

        // native
        mppExtension.targets.withType(KotlinNativeTargetWithHostTests::class) {
            compilations.getByName("main").cinterops {
                for (defFile in defFiles) {
                    create(defFile.name.removeSuffix(".def"))
                }
            }
        }
    }

    private fun addDependencies() {
        val group = "org.jetbrains.mppna"
        val name = "mppna"
        val version = "1.0.0"
        if (mppExtension.targets.all { target -> target is KotlinNativeTargetWithHostTests || target is KotlinJvmTarget }) {
            mppExtension.sourceSets.getByName("commonMain").dependencies {
                implementation("$group:$name:$version")
            }
        } else {
            for (target in mppExtension.targets.filterIsInstance<KotlinJvmTarget>()) {
                mppExtension.sourceSets.getByName("${target.name}Main").dependencies {
                    implementation("$group:$name-jvm:$version")
                }
            }
            for (target in mppExtension.targets.filterIsInstance<KotlinNativeTargetWithHostTests>()) {
                mppExtension.sourceSets.getByName("${target.name}Main").dependencies {
                    implementation("$group:$name-native:$version")
                }
            }
        }
    }

    private fun withJava(withTests: Boolean = true) {
        mppExtension.targets.withType(KotlinJvmTarget::class) {
            withJava()
        }
        project.tasks.withType(JavaExec::class) {
            dependsOn("compileJava")
        }
        if (withTests) {
            project.tasks.withType(Test::class) {
                dependsOn("compileJava")
            }
        }
    }

    override fun apply(project: Project) {
        this.project = project
        mppExtension = project.extensions.findByType(KotlinMultiplatformExtension::class.java) ?: return

        processDefFiles()
        addDependencies()
        withJava()
    }
}