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

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTexture
import de.bixilon.minosoft.gui.rendering.textures.properties.AnimationFrame
import org.testng.Assert.assertEquals
import org.testng.Assert.assertSame
import org.testng.annotations.Test

@Test(groups = ["textures"])
class TextureAnimationTest {
    private val a = DummyTexture()
    private val b = DummyTexture()
    private val c = DummyTexture()

    private fun create(frames: Array<AnimationFrame> = arrayOf(AnimationFrame(0, 0.5f, a), AnimationFrame(1, 1.0f, b), AnimationFrame(2, 2.0f, c))): TextureAnimation {
        return TextureAnimation(0, frames, true, frames.map { it.texture }.toList().toTypedArray())
    }

    fun `first frame`() {
        val animation = create()
        assertSame(animation.frame1, a)
        assertSame(animation.frame2, b)
        assertEquals(animation.progress, 0.0f)
    }

    fun `draw 0,5 frames`() {
        val animation = create()
        animation.update(0.25f)
        assertSame(animation.frame1, a)
        assertSame(animation.frame2, b)
        assertEquals(animation.progress, 0.5f)
    }

    fun `draw 1,0 frames`() {
        val animation = create()
        animation.update(0.5f)
        assertSame(animation.frame1, b)
        assertSame(animation.frame2, c)
        assertEquals(animation.progress, 0.0f)
    }

    fun `draw 1,5 frames`() {
        val animation = create()
        animation.update(1.0f)
        assertSame(animation.frame1, b)
        assertSame(animation.frame2, c)
        assertEquals(animation.progress, 0.5f)
    }

    fun `draw 2,0 frames`() {
        val animation = create()
        animation.update(1.5f)
        assertSame(animation.frame1, c)
        assertSame(animation.frame2, a)
        assertEquals(animation.progress, 0.0f)
    }

    fun `draw 2,5 frames`() {
        val animation = create()
        animation.update(2.5f)
        assertSame(animation.frame1, c)
        assertSame(animation.frame2, a)
        assertEquals(animation.progress, 0.5f)
    }

    fun `draw 3,0 frames`() {
        val animation = create()
        animation.update(3.0f)
        assertSame(animation.frame1, c)
        assertSame(animation.frame2, a)
        assertEquals(animation.progress, 0.75f)
    }

    fun `draw 3,5 frames`() {
        val animation = create()
        animation.update(3.5f)
        assertSame(animation.frame1, a)
        assertSame(animation.frame2, b)
        assertEquals(animation.progress, 0.0f)
    }

    fun `draw 2 frames at once`() {
        val animation = create()
        animation.update(2.0f)
        assertSame(animation.frame1, c)
        assertSame(animation.frame2, a)
        assertEquals(animation.progress, 0.25f)
    }

    fun `draw but just one frame available`() {
        val animation = create(arrayOf(AnimationFrame(0, 1.0f, a)))
        animation.update(2.0f)
        assertSame(animation.frame1, a)
        assertSame(animation.frame2, a)
        assertEquals(animation.progress, 0.0f)
    }

    fun `update 2,5 frames`() {
        val animation = create()
        animation.update(1.0f)
        animation.update(1.0f)
        animation.update(0.5f)
        assertSame(animation.frame1, c)
        assertSame(animation.frame2, a)
        assertEquals(animation.progress, 0.5f)
    }
}

