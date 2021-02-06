package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.entities.Location
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkLocation
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.ChunkPreparer.prepareChunk
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import glm_.vec3.Vec3
import org.lwjgl.Version
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Renderer(private val connection: Connection) {
    private val renderWindow: RenderWindow = RenderWindow(connection)
    private val latch = CountUpAndDownLatch(1)
    private val executor: ExecutorService = Executors.newFixedThreadPool(4, Util.getThreadFactory(String.format("Rendering#%d", connection.connectionId)))

    fun start() {
        Thread({
            Log.info("Hello LWJGL " + Version.getVersion() + "!")
            renderWindow.init(latch)
            renderWindow.startLoop()
            renderWindow.exit()
        }, "Rendering").start()
    }

    fun prepareChunk(chunkLocation: ChunkLocation, chunk: Chunk) {
        renderWindow.chunkSectionsToDraw[chunkLocation] = ConcurrentHashMap()
        for ((sectionHeight, section) in chunk.sections) {
            prepareChunkSection(chunkLocation, sectionHeight, section)
        }
    }

    fun prepareChunkSection(chunkLocation: ChunkLocation, sectionHeight: Int, section: ChunkSection) {
        executor.execute {
            latch.waitUntilZero()
            val data = prepareChunk(connection.player.world, chunkLocation, sectionHeight, section)
            val sectionMap = renderWindow.chunkSectionsToDraw[chunkLocation]!!
            renderWindow.renderQueue.add {
                sectionMap[sectionHeight]?.unload()
                sectionMap.remove(sectionHeight)
                sectionMap[sectionHeight] = Mesh(data, chunkLocation, sectionHeight)
            }
        }
    }

    fun clearCache() {
        renderWindow.renderQueue.add {
            for ((location, map) in renderWindow.chunkSectionsToDraw) {
                for ((sectionHeight, mesh) in map) {
                    mesh.unload()
                    map.remove(sectionHeight)
                }
                renderWindow.chunkSectionsToDraw.remove(location)
            }
        }
    }

    fun teleport(position: Location) {
        renderWindow.renderQueue.add {
            renderWindow.camera.setPosition(Vec3(position.x, position.y, position.z))
        }
    }

}
