/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.AbstractShader
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.IntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.memory.MemoryTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureAnimation
import de.bixilon.minosoft.gui.rendering.textures.properties.AnimationFrame
import de.bixilon.minosoft.gui.rendering.textures.properties.AnimationProperties
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.system.MemoryUtil.memAllocInt
import java.nio.IntBuffer
import kotlin.time.Duration

class SpriteAnimator(val context: RenderContext) {
    private val animations: MutableList<TextureAnimation> = ArrayList()
    private var buffer: IntUniformBuffer? = null
    private var enabled = true
    private var previous = TimeUtil.NULL
    val size get() = animations.size

    fun init() {
        check(animations.size < MAX_ANIMATED_TEXTURES) { "Can not have more than $MAX_ANIMATED_TEXTURES animated textures!" }
        buffer = createBuffer()
        upload()
        context.profile.animations::sprites.observe(this, true) { enabled = it }
    }

    private fun createBuffer(): IntUniformBuffer {
        if (buffer != null) throw IllegalStateException("Already initialized")
        val buffer = context.system.createIntUniformBuffer(memAllocInt(animations.size * INTS_PER_ANIMATED_TEXTURE))
        buffer.init()

        return buffer
    }

    private fun upload() {
        val buffer = this.buffer ?: throw NullPointerException("Buffer not initialized!")
        buffer.upload()
    }

    fun update() {
        val now = now()
        val delta = now - previous
        update(delta)
        previous = now
    }

    private operator fun IntBuffer.set(offset: Int, value: Int) {
        put(offset, value)
    }

    fun update(animation: TextureAnimation, first: Texture, second: Texture, progress: Float) {
        val buffer = buffer!!
        val offset = animation.animationData * INTS_PER_ANIMATED_TEXTURE
        buffer.data[offset + 0] = first.renderData.shaderTextureId
        buffer.data[offset + 1] = second.renderData.shaderTextureId
        buffer.data[offset + 2] = (progress * 100.0f).toInt()
    }

    private fun update(delta: Duration) {
        for (animation in animations) {
            animation.update(delta)

            update(animation, animation.frame1, animation.frame2, animation.progress)
        }
        upload()
    }


    fun use(shader: AbstractShader, bufferName: String = "uSpriteBuffer") {
        buffer!!.use(shader, bufferName)
    }

    @Synchronized
    fun create(texture: Texture, source: TextureBuffer, properties: AnimationProperties): Pair<AnimationProperties.FrameData, TextureAnimation> {
        val data = properties.create(source.size)

        val sprites: Array<Texture> = arrayOfNulls<Texture?>(data.textures).cast()
        for (i in 0 until data.textures) {
            val buffer = source.create(data.size)
            buffer.put(source, Vec2i(0, i * buffer.size.y), Vec2i.EMPTY, data.size)

            sprites[i] = MemoryTexture(size = data.size, texture.properties, texture.mipmaps, buffer)
        }

        val frames: Array<AnimationFrame> = arrayOfNulls<AnimationFrame?>(data.frames.size).cast()

        for ((index, frame) in data.frames.withIndex()) {
            var sprite = sprites.getOrNull(frame.texture)
            if (sprite == null) {
                Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Animation is referencing invalid frame: $texture (frame=${frame.texture})" }
                sprite = sprites.first()
            }
            frames[index] = AnimationFrame(index, frame.time, sprite)
        }

        val animation = TextureAnimation(animations.size, frames, properties.interpolate, sprites)
        this.animations += animation

        return Pair(data, animation)
    }

    fun unload() {
        buffer?.unload()
    }

    companion object {
        const val MAX_ANIMATED_TEXTURES = 1024 // 16kb / 4 (ints per animation) / 4 bytes per int
        private const val INTS_PER_ANIMATED_TEXTURE = 4
    }
}
