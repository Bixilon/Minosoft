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

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.texture.animator.AnimationFrame
import de.bixilon.minosoft.gui.rendering.system.base.texture.animator.SpriteUtil.mapNext
import de.bixilon.minosoft.gui.rendering.system.base.texture.animator.TextureAnimation
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGBA8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTextureLoader
import org.testng.Assert.assertEquals
import org.testng.Assert.assertSame
import org.testng.annotations.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Test(groups = ["textures"])
class TextureAnimationTest {
    private val a = TextureData(RGBA8Buffer(Vec2i(1, 1)))
    private val b = TextureData(RGBA8Buffer(Vec2i(1, 1)))
    private val c = TextureData(RGBA8Buffer(Vec2i(1, 1)))

    private fun create(frames: Array<AnimationFrame> = arrayOf(AnimationFrame(500.milliseconds, a), AnimationFrame(1.seconds, b), AnimationFrame(2.seconds, c))): TextureAnimation {
        frames.mapNext()
        return TextureAnimation(Texture(DummyTextureLoader), frames, true)
    }

    private fun assertSame(frame: AnimationFrame, data: TextureData) {
        assertSame(frame.data, data)
    }

    fun `corrext next frame pointer`() {
        val animation = create()

        assertSame(animation.frame, a)
        assertSame(animation.frame.next, b)
        assertSame(animation.frame.next.next, c)
        assertSame(animation.frame.next.next.next, a)
    }

    fun `first frame`() {
        val animation = create()
        assertSame(animation.frame, a)
        assertEquals(animation.progress, 0.0f)
    }

    fun `draw 0,5 frames`() {
        val animation = create()
        animation.update(0.25f)
        assertSame(animation.frame, a)
        assertEquals(animation.progress, 0.5f)
    }

    fun `draw 1,0 frames`() {
        val animation = create()
        animation.update(0.5f)
        assertSame(animation.frame, b)
        assertEquals(animation.progress, 0.0f)
    }

    fun `draw 1,5 frames`() {
        val animation = create()
        animation.update(1.0f)
        assertSame(animation.frame, b)
        assertEquals(animation.progress, 0.5f)
    }

    fun `draw 2,0 frames`() {
        val animation = create()
        animation.update(1.5f)
        assertSame(animation.frame, c)
        assertEquals(animation.progress, 0.0f)
    }

    fun `draw 2,5 frames`() {
        val animation = create()
        animation.update(2.5f)
        assertSame(animation.frame, c)
        assertEquals(animation.progress, 0.5f)
    }

    fun `draw 3,0 frames`() {
        val animation = create()
        animation.update(3.0f)
        assertSame(animation.frame, c)
        assertEquals(animation.progress, 0.75f)
    }

    fun `draw 3,5 frames`() {
        val animation = create()
        animation.update(3.5f)
        assertSame(animation.frame, a)
        assertEquals(animation.progress, 0.0f)
    }

    fun `draw 2 frames at once`() {
        val animation = create()
        animation.update(2.0f)
        assertSame(animation.frame, c)
        assertEquals(animation.progress, 0.25f)
    }

    fun `draw but just one frame available`() {
        val animation = create(arrayOf(AnimationFrame(1.seconds, a)))
        animation.update(2.0f)
        assertSame(animation.frame, a)
        assertEquals(animation.progress, 0.0f)
    }

    fun `update 2,5 frames`() {
        val animation = create()
        animation.update(1.0f)
        animation.update(1.0f)
        animation.update(0.5f)
        assertSame(animation.frame, c)
        assertEquals(animation.progress, 0.5f)
    }

    private fun TextureAnimation.update(seconds: Float) = update(seconds.toDouble().seconds)
}

