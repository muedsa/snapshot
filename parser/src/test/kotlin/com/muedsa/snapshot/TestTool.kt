package com.muedsa.snapshot

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists

val testImagesDirection: Path = FileSystems.getDefault().getPath("build/test-results/test-image-outputs").apply {
    if (!exists()) {
        createDirectory()
    }
}

val rootDirection: Path = FileSystems.getDefault().getPath("..")


fun getTestPngFile(imagePathWithoutSuffix: String): File {
    var suffix = imagePathWithoutSuffix
    if (imagePathWithoutSuffix.startsWith("/")) {
        suffix = suffix.substring(1)
    }
    val path = testImagesDirection.resolve("$suffix.png")
    path.createParentDirectories()
    return path.toFile()
}