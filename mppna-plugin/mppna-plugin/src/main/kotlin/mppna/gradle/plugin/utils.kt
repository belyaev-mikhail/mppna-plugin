package mppna.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import java.io.File
import java.util.*

class JnaSettings(val defFile: File) {
    val linkerOpts: List<String>
    val jnaLibraryPaths: List<String>

    init {
        val props = Properties()
        props.load(defFile.inputStream())
        linkerOpts = props.getProperty("linkerOpts").split("\\s".toRegex())
        jnaLibraryPaths = linkerOpts.filter { opt -> opt.startsWith("-L")}.map { opt -> opt.removePrefix("-L") }
    }
}

fun Project.processDefFiles(withTests: Boolean = false) {
    val mppExtension = extensions.findByType(KotlinMultiplatformExtension::class.java) ?: return
    val defFiles: List<File> = project.projectDir.resolve("src/nativeInterop/cinterop").listFiles()
            ?.filter { file -> file.name.endsWith(".def") }.orEmpty()
    val jnaSettingsList = defFiles.map { JnaSettings(it) }
    val jnaLibraryPaths = jnaSettingsList.map { defFile -> defFile.jnaLibraryPaths }.flatten().distinct().joinToString(":")

    // jvm
    mppExtension.apply {
        tasks.withType(JavaExec::class) {
            systemProperty("jna.library.path", jnaLibraryPaths)
        }
        if (withTests) {
            tasks.withType(Test::class) {
                systemProperty("jna.library.path", jnaLibraryPaths)
            }
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
