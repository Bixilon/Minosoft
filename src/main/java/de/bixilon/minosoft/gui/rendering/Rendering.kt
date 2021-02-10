package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Location
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import glm_.vec3.Vec3
import org.lwjgl.Version
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Rendering(private val connection: Connection) {
    val renderWindow: RenderWindow = RenderWindow(connection, this)
    val latch = CountUpAndDownLatch(1)
    val executor: ExecutorService = Executors.newFixedThreadPool(4, Util.getThreadFactory(String.format("Rendering#%d", connection.connectionId)))

    fun start() {
        Thread({
            Log.info("Hello LWJGL " + Version.getVersion() + "!")
            renderWindow.init(latch)
            renderWindow.startRenderLoop()
            renderWindow.exit()
        }, "Rendering").start()
    }


    fun teleport(position: Location) {
        renderWindow.renderQueue.add {
            renderWindow.camera.cameraPosition = Vec3(position.x, position.y, position.z)
        }
    }

    fun rotate(rotation: EntityRotation) {
        renderWindow.renderQueue.add {
            renderWindow.camera.setRotation(rotation.yaw.toDouble(), rotation.pitch.toDouble())
        }
    }

}
