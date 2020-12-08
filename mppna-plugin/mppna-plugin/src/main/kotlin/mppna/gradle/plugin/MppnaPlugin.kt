package mppna.gradle.plugin

import mppna.gradle.plugin.generation.MppnaGenerator
import mppna.gradle.plugin.generation.MppnaGeneratorTask
import org.gradle.api.InvalidUserCodeException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultCInteropSettings
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import java.io.File

class MppnaPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("mppnaExtension", MppnaPluginExtension::class.java, project)
    }
}

open class MppnaPluginExtension(private val project: Project) {
    var processAllDefFiles = false
    var processAllTargets = false
    val targets = mutableSetOf<KotlinTarget>()
    val defFiles = mutableSetOf<File>()
    internal val cinterops = mutableListOf<MutableList<DefaultCInteropSettings>>()

    internal fun withJava() {
        val jvmTargetsToConfigure = targets.filterIsInstance<KotlinJvmTarget>()
        if (jvmTargetsToConfigure.size > 1) {
            throw InvalidUserCodeException("Mppna plugin doesn't support bindings' generation for more than one JVM target.")
        }
        jvmTargetsToConfigure.forEach { target ->
            try {
                target.withJava()
            } catch (e: InvalidUserCodeException) {
                throw InvalidUserCodeException("Mppna plugin requires JVM target to be configured to work with Java. " + e.message)
            }
        }
    }

    internal fun processDefFiles(withTests: Boolean = true): List<CinteropSettings> {
        val cinteropSettingsList = defFiles.map { CinteropSettings(it) }
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
        for (defFile in defFiles) {
            cinterops.add(mutableListOf())
            targets.filterIsInstance<KotlinNativeTargetWithHostTests>().forEach { target ->
                target.apply {
                    compilations.getByName("main").cinterops {
                        val lib = create(defFile.name.removeSuffix(".def"))
                        lib.extraOpts.addAll(listOf("-mode", "sourcecode"))
                        cinterops.last().add(lib)
                    }
                }
            }
        }
        return cinteropSettingsList
    }

    companion object {
        const val CINTEROP_DIR = "src/nativeInterop/cinterop"
        const val JNAERATOR_DIR = "generatedSources/jnaerator"
        const val MPPNA_DECLARATIONS_DIR = "generatedSources/mppna"
    }
}

fun Project.mppna(configure: MppnaPluginExtension.() -> Unit) {
    val mppExtension = extensions.findByType(KotlinMultiplatformExtension::class.java)
            ?: error("Mppna plugin only supports kotlin multiplatform projects")
    val extension = extensions.findByType(MppnaPluginExtension::class.java)
            ?: extensions.create("mppnaExtension", MppnaPluginExtension::class.java, project)

    extension.configure()
    if (extension.processAllDefFiles) {
        extension.defFiles.addAll(
                project.projectDir
                        .resolve(MppnaPluginExtension.CINTEROP_DIR).listFiles()
                        ?.filter { file -> file.name.endsWith(".def") }.orEmpty()
        )
    }
    if (extension.processAllTargets) {
        extension.targets.addAll(mppExtension.targets.filter { target ->
            target is KotlinJvmTarget || target is KotlinNativeTargetWithHostTests
        })
    }

    if (extension.targets.none { it is KotlinNativeTargetWithHostTests })
        error("Targets need to include at least one native target.")
    extension.withJava()
    val cinteropSettingsList = extension.processDefFiles()

    val groupName = "generation"
    val sourceSets = project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
    val mainSourceSet: SourceSet = sourceSets.getByName("main")
    mainSourceSet.java.srcDir(File(project.buildDir, MppnaPluginExtension.JNAERATOR_DIR))

    val targetsToSrcDirs = mutableMapOf<KotlinTarget?, File>()
    for (target in extension.targets + null) {
        val name = target?.name ?: "common"
        val srcDir = project.buildDir.resolve("${MppnaPluginExtension.MPPNA_DECLARATIONS_DIR}/$name")
        mppExtension.sourceSets.getByName("${name}Main").kotlin.srcDir(srcDir)
        targetsToSrcDirs[target] = srcDir
    }

    for ((i, cinteropSettings) in cinteropSettingsList.withIndex()) {
        val mppnaPackage = "${cinteropSettings.libraryKN}${MppnaGenerator.MPPNA_PACKAGE_NAME.capitalize()}"
        val outputDirJna = project.buildDir.resolve("${MppnaPluginExtension.JNAERATOR_DIR}/$mppnaPackage")
        val jnaeratorTask = project.tasks.create("jnaerator${cinteropSettings.libraryKN.capitalize()}",
                JNAeratorTask::class.java, outputDirJna, cinteropSettings.libraryC, cinteropSettings.libraryKN, cinteropSettings.headers)
        jnaeratorTask.group = groupName

        val nativeTarget = extension.targets.firstIsInstance<KotlinNativeTargetWithHostTests>()

        val klibSourceCodeFile = project.buildDir.resolve(
                "classes/kotlin/${nativeTarget.name}/main/" +
                        "${project.name}-cinterop-${cinteropSettings.libraryKN}.klib-build/" +
                        "kotlin/${cinteropSettings.libraryKN}/${cinteropSettings.libraryKN}.kt")
        val outputDirsMppna = targetsToSrcDirs.values.map { it.resolve(mppnaPackage) }.toList()

        val mppnaGeneratorTask = project.tasks.create("mppna${cinteropSettings.libraryKN.capitalize()}",
                MppnaGeneratorTask::class.java, outputDirsMppna, klibSourceCodeFile,
                cinteropSettings.libraryC, cinteropSettings.libraryKN, targetsToSrcDirs)
        mppnaGeneratorTask.group = groupName
        mppnaGeneratorTask.dependsOn(jnaeratorTask, *extension.cinterops[i].map { it.interopProcessingTaskName }.toTypedArray())
    }
}