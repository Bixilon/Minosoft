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

package de.bixilon.minosoft.gui.rendering.system.base.texture.animator

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.animator.SpriteUtil.mapNext
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.properties.AnimationProperties
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.time.Duration

class SpriteAnimator(val context: RenderContext) {
    private val animations: MutableList<TextureAnimation> = ArrayList()
    private var enabled = true
    private var previous = TimeUtil.NULL
    val size get() = animations.size

    fun init() {
        context.profile.animations::sprites.observe(this, true) { enabled = it }
    }

    fun update() {
        if (!enabled) return
        val now = now()
        val delta = now - previous
        update(delta)
        previous = now
    }

    fun update(animation: TextureAnimation) {
        val destination = animation.data.collect()
        val a = animation.frame.data.collect()
        val b = animation.frame.next.data.collect()

        assert(destination.size == a.size && destination.size == b.size)


        for (index in 0 until destination.size) {
            destination[index].interpolate(a[index], b[index], if (animation.interpolate) animation.progress else 0.0f)
        }

        // TODO: context.textures.static.update(animation.texture)
    }

    private fun update(delta: Duration) {
        for (animation in animations) {
            animation.update(delta)

            update(animation)
        }
    }

    fun create(texture: Texture, source: TextureBuffer, properties: AnimationProperties): TextureAnimation {
        val data = properties.create(source.size)

        val sprites: Array<TextureBuffer> = arrayOfNulls<TextureBuffer?>(data.textures).cast()
        for (i in 0 until data.textures) {
            val buffer = source.create(data.size)
            buffer.put(source, Vec2i(0, i * buffer.size.y), Vec2i.EMPTY, data.size)

            sprites[i] = buffer
        }

        val frames: Array<AnimationFrame> = arrayOfNulls<AnimationFrame?>(data.frames.size).cast()

        for ((index, frame) in data.frames.withIndex()) {
            var sprite = sprites.getOrNull(frame.texture)
            if (sprite == null) {
                Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Animation is referencing invalid frame: $texture (frame=${frame.texture})" }
                sprite = sprites.first()
            }
            frames[index] = AnimationFrame(frame.time, texture.createData(buffer = sprite))
        }

        frames.mapNext()


        val animation = TextureAnimation(texture, frames, properties.interpolate)

        synchronized(this) {
            this.animations += animation
        }

        return animation
    }
}
