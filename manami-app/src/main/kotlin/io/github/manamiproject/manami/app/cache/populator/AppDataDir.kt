package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.modb.core.extensions.directoryExists
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectory

fun appDataDir(path: Path): Path {
    val home = System.getProperty("user.home") ?: throw IllegalStateException("user.home not set")
    val appDir = Path(home).resolve(".manami")

    if (!appDir.directoryExists()) {
        appDir.createDirectory()
    }

    return appDir.resolve(path)
}