/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.base.texture.sprite

import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.IntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.textures.TextureAnimation

class SpriteAnimator(val renderSystem: RenderSystem) {
    val animations: MutableList<TextureAnimation> = mutableListOf()
    val size: Int
        get() = animations.size
    private lateinit var uniformBuffer: IntUniformBuffer
    var lastRun = 0L

    var initialized = false
        private set
    var enabled = true

    fun init() {
        check(animations.size < MAX_ANIMATED_TEXTURES) { "Can not have more than $MAX_ANIMATED_TEXTURES animated textures!" }
        uniformBuffer = renderSystem.createIntUniformBuffer(IntArray(animations.size * INTS_PER_ANIMATED_TEXTURE))
        uniformBuffer.init()
        initialized = true
        recalculate()
        uniformBuffer.upload()
    }

    private fun recalculate() {
        val currentTime = millis()
        val deltaLastDraw = currentTime - lastRun
        lastRun = currentTime

        for (textureAnimation in animations) {
            var currentFrame = textureAnimation.getCurrentFrame()
            textureAnimation.currentTime += deltaLastDraw

            if (textureAnimation.currentTime >= currentFrame.animationTime) {
                currentFrame = textureAnimation.getAndSetNextFrame()
                textureAnimation.currentTime = 0L
            }

            val nextFrame = textureAnimation.getNextFrame()

            val interpolation = if (textureAnimation.animationProperties.interpolate) {
                (textureAnimation.currentTime * 100) / currentFrame.animationTime
            } else {
                0L
            }


            val arrayOffset = textureAnimation.texture.renderData.animationData * INTS_PER_ANIMATED_TEXTURE

            uniformBuffer.data[arrayOffset] = currentFrame.texture.renderData.shaderTextureId
            uniformBuffer.data[arrayOffset + 1] = nextFrame.texture.renderData.shaderTextureId
            uniformBuffer.data[arrayOffset + 2] = interpolation.toInt()
        }
    }

    fun draw() {
        if (!initialized || !enabled) {
            return
        }
        recalculate()
        uniformBuffer.upload()
    }


    fun use(shader: NativeShader, bufferName: String = "uSpriteBuffer") {
        uniformBuffer.use(shader, bufferName)
    }

    companion object {
        const val MAX_ANIMATED_TEXTURES = 1024 // 16kb / 4 (ints per animation) / 4 bytes per int
        private const val INTS_PER_ANIMATED_TEXTURE = 4
    }
}
