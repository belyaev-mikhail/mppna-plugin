package mppna.gradle.plugin

import com.ochafik.lang.jnaerator.JNAerator
import com.ochafik.lang.jnaerator.JNAeratorCommandLineArgs
import com.ochafik.lang.jnaerator.JNAeratorConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class JNAeratorTask @Inject constructor(
        @OutputDirectory var outputDir: File,
        private val library: String,
        private val headers: List<String>
) : DefaultTask() {

    @TaskAction
    fun generate() {
        val args = mutableListOf<String>()

        args.add(JNAeratorCommandLineArgs.OptionDef.CurrentLibrary.clSwitch)
        args.add(library)
        args.add(JNAeratorCommandLineArgs.OptionDef.CurrentPackage.clSwitch)
        args.add("lib${library}Mppna")

        for (header in headers)
            args.add(header)

        args.add(JNAeratorCommandLineArgs.OptionDef.OutputMode.clSwitch)
        args.add(JNAeratorConfig.OutputMode.Directory.name)

        args.add(JNAeratorCommandLineArgs.OptionDef.OutputDir.clSwitch)
        args.add(outputDir.absolutePath)

        args.add(JNAeratorCommandLineArgs.OptionDef.Runtime.clSwitch)
        args.add(JNAeratorConfig.Runtime.JNA.name)

        args.add(JNAeratorCommandLineArgs.OptionDef.ForceOverwrite.clSwitch)

        JNAerator.main(args.toTypedArray())

    }
}