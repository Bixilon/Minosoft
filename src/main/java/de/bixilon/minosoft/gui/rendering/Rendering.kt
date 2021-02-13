package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import org.lwjgl.Version
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Rendering(private val connection: Connection) {
    val renderWindow: RenderWindow = RenderWindow(connection, this)
    val executor: ExecutorService = Executors.newFixedThreadPool(4, Util.getThreadFactory(String.format("Rendering#%d", connection.connectionId)))

    fun start(latch: CountUpAndDownLatch) {
        latch.countUp()
        Thread({
            Log.info("Hello LWJGL " + Version.getVersion() + "!")
            renderWindow.init(latch)
            renderWindow.startRenderLoop()
            renderWindow.exit()
        }, "Rendering").start()
    }
}
