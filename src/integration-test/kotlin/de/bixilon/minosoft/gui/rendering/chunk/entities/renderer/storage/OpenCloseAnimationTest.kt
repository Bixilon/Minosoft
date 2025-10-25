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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.primitive.FloatUtil.matches
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalTransform
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.NOT_OVER
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.OVER
import de.bixilon.minosoft.gui.rendering.skeletal.instance.SkeletalInstance
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["skeletal", "block_entity_rendering"])
class OpenCloseAnimationTest {

    private fun create(): Animation {
        val mesh = Mesh::class.java.allocate()
        val context = RenderContext::class.java.allocate()
        val model = BakedSkeletalModel(mesh, BakedSkeletalTransform(0, Vec3f.EMPTY, emptyMap()), 1, emptyMap())
        val instance = model.createInstance(context)

        return Animation(instance)
    }

    fun `create animation`() {
        create()
    }

    fun `correct playing and over state`() {
        val animation = create()
        assertFalse(animation.getInstance().animation.isPlaying(animation))
        animation.open()
        assertTrue(animation.getInstance().animation.isPlaying(animation))
        animation.close()
        assertTrue(animation.getInstance().animation.isPlaying(animation))
        assertEquals(animation.draw(0.3f), OVER)
        // assertFalse(animation.getInstance().animation.isPlaying(animation)) // animation is drawn directly, that is part of the animation manager
    }

    fun animation() {
        val animation = create()
        animation.open()

        assertEquals(animation.getProgress(), 0.0f)
        assertEquals(animation.draw(0.1f), NOT_OVER)
        assertEquals(animation.getProgress(), 0.2f)
        assertEquals(animation.draw(0.2f), NOT_OVER)
        assertEquals(animation.getProgress(), 0.6f)
        assertEquals(animation.draw(0.3f), NOT_OVER)
        assertEquals(animation.getProgress(), 1.0f)

        assertEquals(animation.draw(0.3f), NOT_OVER)
        assertEquals(animation.getProgress(), 1.0f)

        animation.close()

        assertEquals(animation.draw(0.1f), NOT_OVER)
        assertEquals(animation.getProgress(), 2.0f / 3.0f)
        assertEquals(animation.draw(0.1f), NOT_OVER)
        assertTrue(animation.getProgress().matches(1.0f / 3.0f))
        assertEquals(animation.draw(0.05f), NOT_OVER)
        assertTrue(animation.getProgress().matches(1.0f / 6.0f))

        assertEquals(animation.draw(0.06f), OVER)
        assertEquals(animation.getProgress(), 0.0f)
    }

    private class Animation(
        instance: SkeletalInstance,
    ) : OpenCloseAnimation(instance) {
        override val transform = instance.transform

        override val name get() = "dummy"

        override val closingDuration get() = 0.3f
        override val openingDuration get() = 0.5f


        override fun transform() = Unit

        @JvmName("getProgress2")
        fun getProgress() = this.progress

        @JvmName("getInstance2")
        fun getInstance() = this.instance
    }
}
