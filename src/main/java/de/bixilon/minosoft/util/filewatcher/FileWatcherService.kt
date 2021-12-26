package de.bixilon.minosoft.util.filewatcher

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.nio.file.*
import kotlin.io.path.pathString


object FileWatcherService {
    private val WATCHERS: SynchronizedMap<WatchKey, SynchronizedMap<String, FileWatcher>> = synchronizedMapOf()
    private var stop = false
    private var service: WatchService? = null
    private lateinit var thread: Thread

    fun start() {
        if (service != null) {
            throw IllegalStateException("Already running!")
        }
        this.stop = false
        val latch = CountUpAndDownLatch(1)
        this.thread = Thread({
            try {
                val service = FileSystems.getDefault().newWatchService()
                this.service = service
                latch.dec()
                while (true) {
                    val watchKey: WatchKey = service.take()
                    if (stop) {
                        break
                    }
                    for (event in watchKey.pollEvents()) {
                        val path = event.context().nullCast<Path>() ?: continue

                        WATCHERS[watchKey]?.let { notifyChange(it, event, path.pathString) }
                    }
                    watchKey.reset()
                }
                for (watchKey in WATCHERS.keys) {
                    watchKey.cancel()
                }
                WATCHERS.clear()
            } catch (ignored: InterruptedException) {
            } finally {
                Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Stopping file watcher service" }
                if (latch.count > 0) {
                    latch.dec()
                }
                this.service = null
            }
        }, "FileWatcherService")
        thread.start()


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

    fun stop() {
        if (service == null) {
            return
        }
        stop = true
        thread.interrupt()
    }
}
