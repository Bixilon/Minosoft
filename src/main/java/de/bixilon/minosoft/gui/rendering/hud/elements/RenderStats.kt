package de.bixilon.minosoft.gui.rendering.hud.elements

class RenderStats {
    var fpsLastSecond = -1
    var minFrameTime = Long.MAX_VALUE
    var maxFrameTime = 0L
    var avgFrameTime = 0L
    var frames = 0L

    private var lastFPSCalcTime = 0L
    private var framesLastSecond = 0

    private var frameStartTime = 0L

    fun startFrame() {
        frameStartTime = System.nanoTime()
    }

    fun endDraw() {
    }

    fun endFrame() {
        val frameEndTime = System.nanoTime()
        val frameTime = frameEndTime - frameStartTime
        if (frameTime < minFrameTime) {
            minFrameTime = frameTime
        }
        if (frameTime > maxFrameTime) {
            maxFrameTime = frameTime
        }

        if (frameEndTime - lastFPSCalcTime > 1E9) {
            // 1 second
            fpsLastSecond = framesLastSecond

            framesLastSecond = 0
            lastFPSCalcTime = frameEndTime
        }
        frames++
        framesLastSecond++

    }
}
