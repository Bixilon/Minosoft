package de.bixilon.minosoft.gui.rendering.hud.elements.text

import de.bixilon.minosoft.gui.rendering.font.FontBindings

class HUDDebugScreenElement(private val hudTextElement: HUDTextElement) : HUDText {
    private val runtime = Runtime.getRuntime()

    override fun prepare(chatComponents: Map<FontBindings, MutableList<Any>>) {
        chatComponents[FontBindings.LEFT_UP]!!.addAll(listOf(
            "§fFPS: ${getFPS()}",
            "§fTimings: avg ${getAvgFrameTime()}ms, min ${getMinFrameTime()}ms, max ${getMaxFrameTime()}ms",
            "§fXYZ ${getLocation()}",
            "§fConnected to: ${hudTextElement.connection.address}",
        ))
        chatComponents[FontBindings.RIGHT_UP]!!.addAll(listOf(
            "§fJava: ${Runtime.version()} ${System.getProperty("sun.arch.data.model")}bit",
            "§fMemory: ${getUsedMemoryPercent()}% ${getFormattedUsedMemory()}/${getFormattedMaxMemory()}",
            "§fAllocated: ${getAllocatedMemoryPercent()}% ${getFormattedAllocatedMemory()}",
            "",
            "CPU: ${runtime.availableProcessors()}x TODO",
            "OS: ${System.getProperty("os.name")}",
            "",
            "Display: ${getScreenDimensions()}",
        ))
    }

    private fun nanoToMillis1d(nanos: Long): String {
        return "%.1f".format(nanos / 1000000f)
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

    private fun getLocation(): String {
        val cameraPosition = hudTextElement.renderWindow.camera.cameraPosition
        return "${formatCoordinate(cameraPosition.x)} / ${formatCoordinate(cameraPosition.y)} / ${formatCoordinate(cameraPosition.z)}"
    }

    private fun getScreenDimensions(): String {
        return "${hudTextElement.renderWindow.screenWidth}x${hudTextElement.renderWindow.screenHeight}"
    }


    companion object {
        private val UNITS = listOf("B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB")
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

        fun formatCoordinate(coordinate: Float): String {
            return "%.4f".format(coordinate)
        }
    }
}
