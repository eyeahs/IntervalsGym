package com.lighthousepark.intervalsgym

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

internal object ArchitectureGuardProject {
    val projectRoot: Path = findProjectRoot()
    val mainSourceRoot: Path = projectRoot.resolve("app/src/main/java")
    val testSourceRoot: Path = projectRoot.resolve("app/src/test/java")
    val androidTestSourceRoot: Path = projectRoot.resolve("app/src/androidTest/java")

    private fun findProjectRoot(): Path {
        return generateSequence(Paths.get("").toAbsolutePath()) { path -> path.parent }
            .first { path ->
                Files.exists(path.resolve("settings.gradle.kts")) ||
                    Files.exists(path.resolve("settings.gradle"))
            }
    }
}

internal fun kotlinFiles(root: Path): List<Path> {
    if (!Files.exists(root)) return emptyList()
    return Files.walk(root).use { paths ->
        paths
            .filter { path -> Files.isRegularFile(path) && path.toString().endsWith(".kt") }
            .toList()
    }
}

internal fun Path.relativeToProject(): String {
    return ArchitectureGuardProject.projectRoot.relativize(this).toString()
}
