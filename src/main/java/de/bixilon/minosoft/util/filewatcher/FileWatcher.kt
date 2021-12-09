package de.bixilon.minosoft.util.filewatcher

import java.nio.file.Path
import java.nio.file.WatchEvent

class FileWatcher(
    val path: Path,
    val types: Array<WatchEvent.Kind<*>>,
    val callback: (WatchEvent<*>, String) -> Unit,
)
