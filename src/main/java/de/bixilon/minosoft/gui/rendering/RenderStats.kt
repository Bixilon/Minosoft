/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering

class RenderStats {
    var fpsLastSecond = -1
        private set
    var minFrameTime = Long.MAX_VALUE
        private set
    var maxFrameTime = 0L
        private set
    var avgFrameTime = 0L
        private set
    var frames = 0L
        private set

    private var lastFPSCalcTime = 0L
    private var framesLastSecond = 0
    private var frameTime = 0L

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
            this.frameTime = 0
        }
        frames++
        framesLastSecond++

        this.frameTime += frameTime
        this.avgFrameTime = this.frameTime / framesLastSecond
    }
}
