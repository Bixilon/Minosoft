package de.bixilon.minosoft.gui.rendering.hud.elements.text

import de.bixilon.minosoft.data.entities.Location
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.data.world.ChunkLocation
import de.bixilon.minosoft.data.world.InChunkSectionLocation
import de.bixilon.minosoft.gui.rendering.font.FontBindings
import de.bixilon.minosoft.modding.loading.ModLoader
import org.lwjgl.opengl.GL11.*
import oshi.SystemInfo


class HUDDebugScreenElement(private val hudTextElement: HUDTextElement) : HUDText {
    private val runtime = Runtime.getRuntime()
    private val systemInfo = SystemInfo()
    private val systemInfoHardwareAbstractionLayer = systemInfo.hardware


    private val processorText = " ${runtime.availableProcessors()}x ${systemInfoHardwareAbstractionLayer.processor.processorIdentifier.name.replace("\\s{2,}".toRegex(), "")}"
    private lateinit var gpuText: String
    private lateinit var gpuVersionText: String
    private val maxMemoryText: String = getFormattedMaxMemory()
    private val systemMemoryText: String = formatBytes(systemInfoHardwareAbstractionLayer.memory.total)
    private val osText: String = "${System.getProperty("os.name")}: ${systemInfo.operatingSystem.family} ${systemInfo.operatingSystem.bitness}bit"

    // only calculate once per update values
    private lateinit var location: Location
    private lateinit var blockPosition: BlockPosition
    private lateinit var chunkLocation: ChunkLocation
    private var sectionHeight: Int = 0
    private lateinit var inChunkSectionLocation: InChunkSectionLocation

    override fun prepare(chatComponents: Map<FontBindings, MutableList<Any>>) {
        calculateDynamicValues()

        chatComponents[FontBindings.LEFT_UP]!!.addAll(listOf(
            "FPS: ${getFPS()}",
            "Timings: avg ${getAvgFrameTime()}ms, min ${getMinFrameTime()}ms, max ${getMaxFrameTime()}ms",
            "Connected to ${hudTextElement.connection.address} with ${hudTextElement.connection.version}",
            "",
            "XYZ ${getLocation()}",
            "Block ${getBlockPosition()}",
            "Chunk \n${getChunkLocation()}",
            "Facing ${getFacing()}",
            "Dimension ${hudTextElement.connection.player.world.dimension}",
        ))
        chatComponents[FontBindings.RIGHT_UP]!!.addAll(listOf(
            "Java: ${Runtime.version()} ${System.getProperty("sun.arch.data.model")}bit",
            "Memory: ${getUsedMemoryPercent()}% ${getFormattedUsedMemory()}/$maxMemoryText",
            "Allocated: ${getAllocatedMemoryPercent()}% ${getFormattedAllocatedMemory()}",
            "System: $systemMemoryText",
            "",
            "OS:\n $osText",
            "CPU: $processorText",
            "",
            "Display: ${getScreenDimensions()}",
            "GPU: $gpuText",
            "Version: $gpuVersionText",
            "",
            "Mods: ${ModLoader.MOD_MAP.size} active, ${hudTextElement.connection.eventListenerSize} listeners",
        ))
    }

    override fun init() {
        gpuText = glGetString(GL_RENDERER) ?: "unknown"
        gpuVersionText = glGetString(GL_VERSION) ?: "unknown"
    }

    private fun nanoToMillis1d(nanos: Long): String {
        return "%.1f".format(nanos / 1E6f)
    }

    private fun getFPS(): String {
        val renderStats = hudTextElement.renderWindow.renderStats
        return "${renderStats.fpsLastSecond}"
    }

    private fun getAvgFrameTime(): String {
        return nanoToMillis1d(hudTextElement.renderWindow.renderStats.avgFrameTime)
    }

    private fun getMinFrameTime(): String {
        return nanoToMillis1d(hudTextElement.renderWindow.renderStats.minFrameTime)
    }

    private fun getMaxFrameTime(): String {
        return nanoToMillis1d(hudTextElement.renderWindow.renderStats.maxFrameTime)
    }

    private fun getUsedMemory(): Long {
        return runtime.totalMemory() - runtime.freeMemory()
    }

    private fun getFormattedUsedMemory(): String {
        return formatBytes(getUsedMemory())
    }

    private fun getAllocatedMemory(): Long {
        return runtime.totalMemory()
    }

    private fun getFormattedAllocatedMemory(): String {
        return formatBytes(getAllocatedMemory())
    }

    private fun getMaxMemory(): Long {
        return runtime.maxMemory()
    }

    private fun getFormattedMaxMemory(): String {
        return formatBytes(getMaxMemory())
    }

    private fun getUsedMemoryPercent(): Long {
        return getUsedMemory() * 100 / runtime.maxMemory()
    }

    private fun getAllocatedMemoryPercent(): Long {
        return getAllocatedMemory() * 100 / runtime.maxMemory()
    }

    private fun getScreenDimensions(): String {
        return "${hudTextElement.renderWindow.screenWidth}x${hudTextElement.renderWindow.screenHeight}"
    }

    private fun getLocation(): String {
        return "${formatCoordinate(location.x)} / ${formatCoordinate(location.y)} / ${formatCoordinate(location.z)}"
    }

    private fun getBlockPosition(): String {
        return "${blockPosition.x} / ${blockPosition.y} / ${blockPosition.z}"
    }

    private fun getChunkLocation(): String {
        return "${inChunkSectionLocation.x} ${inChunkSectionLocation.y} ${inChunkSectionLocation.z} in ${chunkLocation.x} $sectionHeight ${chunkLocation.z}"
    }

    private fun getFacing(): String {
        val yaw = hudTextElement.renderWindow.camera.yaw
        val pitch = hudTextElement.renderWindow.camera.pitch
        return "todo (${formatRotation(yaw)} / ${formatRotation(pitch)})"
    }

    private fun calculateDynamicValues() {
        location = Location(hudTextElement.renderWindow.camera.cameraPosition)
        blockPosition = location.toBlockPosition()
        chunkLocation = blockPosition.getChunkLocation()
        sectionHeight = blockPosition.getSectionHeight()
        inChunkSectionLocation = blockPosition.getInChunkSectionLocation()
    }


    companion object {
        private val UNITS = listOf("B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB")
        fun formatBytes(bytes: Long): String {
            var lastFactor = 1L
            var currentFactor = 1024L
            for (unit in UNITS) {
                if (bytes < currentFactor) {
                    if (bytes < (lastFactor * 10)) {
                        return "${"%.1f".format(bytes / lastFactor.toFloat())}${unit}"
                    }
                    return "${bytes / lastFactor}${unit}"
                }
                lastFactor = currentFactor
                currentFactor *= 1024L
            }
            throw IllegalArgumentException()
        }

        fun formatCoordinate(coordinate: Double): String {
            return "%.3f".format(coordinate)
        }

        fun formatRotation(rotation: Double): String {
            return "%.1f".format(rotation)
        }
    }
}
