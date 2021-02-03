package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.entities.Location
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkLocation
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.ChunkPreparer.prepareChunk
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.util.Util
import glm_.vec3.Vec3
import org.lwjgl.Version
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Renderer(private val connection: Connection) {
    private val renderWindow: RenderWindow = RenderWindow(connection)
    private val executor: ExecutorService = Executors.newFixedThreadPool(2, Util.getThreadFactory(String.format("Rendering#%d", connection.connectionId)))

    fun start() {
        Thread({
            println("Hello LWJGL " + Version.getVersion() + "!")
            renderWindow.init()
            renderWindow.startLoop()
            renderWindow.exit()
        }, "Rendering").start()
    }

    fun prepareChunk(chunkLocation: ChunkLocation, chunk: Chunk) {
        for ((sectionHeight, section) in chunk.sections) {
            prepareChunkSection(chunkLocation, sectionHeight, section)
        }
    }

    fun prepareChunkSection(chunkLocation: ChunkLocation, sectionHeight: Int, section: ChunkSection) {
        executor.execute {
            val data = prepareChunk(connection.player.world, chunkLocation, sectionHeight, section)
            renderWindow.renderQueue.add {
                renderWindow.meshesToDraw.add(Mesh(data, chunkLocation, sectionHeight))
            }
        }
    }

    fun teleport(position: Location) {
        renderWindow.renderQueue.add {
            renderWindow.camera.setPosition(Vec3(position.x, position.y, position.z))

        }
    }

}
