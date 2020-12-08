package mppna.gradle.plugin

import java.io.File
import java.util.*


class CinteropSettings(defFile: File) {
    val linkerOpts: List<String>
    val compilerOpts: List<String>
    val jnaLibraryPaths: List<String>
    val libraryC: String
    val headers: List<String>
    val libraryKN: String

    init {
        val props = Properties()
        props.load(defFile.inputStream())

        linkerOpts = (props["linkerOpts"] as? String)?.split("\\s".toRegex()).orEmpty()
        compilerOpts = (props["compilerOpts"] as? String)?.split("\\s".toRegex()).orEmpty()
        jnaLibraryPaths = linkerOpts.filter { opt -> opt.startsWith("-L") }.map { opt -> opt.removePrefix("-L") }

        // TODO: support several library files
        libraryC = linkerOpts.filter { opt -> opt.startsWith("-l") }.map { opt -> opt.removePrefix("-l") }.first()
        libraryKN = defFile.name.removeSuffix(".def")

        val includePath = compilerOpts.first { it.startsWith("-I") }.removePrefix("-I")
        headers = (props["headers"] as? String)?.split("\\s".toRegex()).orEmpty().map { "$includePath/$it" }
    }
}