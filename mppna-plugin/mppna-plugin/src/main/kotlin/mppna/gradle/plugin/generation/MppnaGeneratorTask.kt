package mppna.gradle.plugin.generation

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.File
import javax.inject.Inject

open class MppnaGeneratorTask @Inject constructor(
        @OutputDirectories var outputDirs: List<File>,
        @InputFile private val klibSourceCodeFile: File,
        private val libraryC: String,
        private val libraryKN: String,
        private val targetsToSrcDirs: Map<KotlinTarget?, File>
) : DefaultTask() {
    @TaskAction
    fun generate() {
        val generator = MppnaGenerator(libraryC, libraryKN, klibSourceCodeFile)
        generator.generateDeclarations(targetsToSrcDirs)
    }
}