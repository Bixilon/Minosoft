package de.bixilon.minosoft.util.filewatcher

import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.collections.SynchronizedMap
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import java.nio.file.*
import kotlin.io.path.pathString


object FileWatcherService {
    private val WATCHERS: SynchronizedMap<WatchKey, SynchronizedMap<String, FileWatcher>> = synchronizedMapOf()
    private var running = false
    private var service: WatchService? = null

    fun start() {
        if (running) {
            throw IllegalStateException("Already running!")
        }
        val latch = CountUpAndDownLatch(1)
        Thread({
            try {
                val service = FileSystems.getDefault().newWatchService()
                this.service = service
                latch.dec()
                while (true) {
                    val watchKey: WatchKey = service.take()
                    for (event in watchKey.pollEvents()) {
                        val path = event.context().nullCast<Path>() ?: continue

                        WATCHERS[watchKey]?.let { notifyChange(it, event, path.pathString) }
                    }
                    watchKey.reset()
                }
            } finally {
                Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Stopping file watcher service" }
                if (latch.count > 0) {
                    latch.dec()
                }
                this.service = null
            }
        }, "FileWatcherService").start()


        latch.await()
    }

    private fun notifyChange(watchers: SynchronizedMap<String, FileWatcher>, event: WatchEvent<*>, fileName: String) {
        try {
            watchers[fileName]?.callback?.invoke(event, fileName) ?: return
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }

    fun register(fileWatcher: FileWatcher) {
        DefaultThreadPool += { registerWait(fileWatcher) }
    }

    fun registerWait(fileWatcher: FileWatcher) {
        val service = this.service ?: throw IllegalStateException("File watcher service not running!")
        var watchKey: WatchKey? = null
        try {
            val file = fileWatcher.path.toFile()
            var parent = file
            if (parent.isFile) {
                // Can not register file listener, only directory
                parent = parent.parentFile
            }
            if (!parent.isDirectory) {
                throw IllegalStateException("Can not watch directory: ${parent.path}")
            }
            val parentPath = parent.toPath()

            watchKey = parentPath.register(service, *fileWatcher.types)
            WATCHERS.getOrPut(watchKey) { synchronizedMapOf() }[file.path.removePrefix(parentPath.pathString).removePrefix("/")] = fileWatcher
        } catch (exception: Exception) {
            WATCHERS.remove(watchKey)
            throw exception
        }
    }
}
