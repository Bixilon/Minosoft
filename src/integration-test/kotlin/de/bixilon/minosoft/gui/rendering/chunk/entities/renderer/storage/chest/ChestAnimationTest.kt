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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.storage.chest

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.primitive.FloatUtil.matches
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.skeletal.SkeletalMesh
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalTransform
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.NOT_OVER
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.OVER
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["skeletal", "block_entity_rendering"])
class ChestAnimationTest {
    private val progress = ChestAnimation::class.java.getDeclaredField("progress").apply { isAccessible = true }

    private fun ChestAnimation.getProgress(): Float = progress.getFloat(this)

    private fun create(): ChestAnimation {
        val mesh = IT.OBJENESIS.newInstance(SkeletalMesh::class.java)
        val context = IT.OBJENESIS.newInstance(RenderContext::class.java)
        val model = BakedSkeletalModel(mesh, BakedSkeletalTransform(0, Vec3.EMPTY, mapOf("lid" to BakedSkeletalTransform(1, Vec3.EMPTY, emptyMap()))), 1, emptyMap())
        val instance = model.createInstance(context)

        return ChestAnimation(instance)
    }

    fun `create animation`() {
        create()
    }

    fun `correct playing and over state`() {
        val animation = create()
        // TODO: assert not playing
        animation.open()
        // TODO: assert playing
        animation.close()
        // TODO: assert playing
        assertEquals(animation.draw(0.3f), OVER)
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

    // TODO: test transforming
}
