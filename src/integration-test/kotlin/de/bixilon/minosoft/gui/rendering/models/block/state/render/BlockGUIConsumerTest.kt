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

package de.bixilon.minosoft.gui.rendering.models.block.state.render

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIMeshCache
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.raw.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.models.util.CuboidUtil
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.MeshOrder
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.annotations.Test

@Test(groups = ["models"])
class BlockGUIConsumerTest {

    private fun create(): BlockGUIConsumer {
        val gui = GUIRenderer::class.java.allocate()
        val consumer = GUIConsumer()

        return BlockGUIConsumer(gui, Vec2f(11, 12), consumer, null, ModelDisplay.DEFAULT, Vec2f(45))
    }

    private fun BlockGUIConsumer.assertVertices(expected: Array<Vec2f>) {
        val consumer = this.consumer.unsafeCast<GUIConsumer>()

        if (consumer.vertices.size != expected.size) throw AssertionError("Size mismatch")

        for (index in expected.indices) {
            val delta = expected[index] - consumer.vertices[index]
            if (delta.length2() < 0.01f) continue
            throw AssertionError("Vertex at index $index mismatched: Expected ${expected[index]}, but got ${consumer.vertices[index]}")
        }
    }


    @Test(enabled = false)
    fun `south quad with offset and specific size`() {
        val consumer = create()
        val position = CuboidUtil.positions(Directions.SOUTH, Vec3f(0, 0, 0), Vec3f(1, 1, 1))
        val uv = UnpackedUV(floatArrayOf(0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f))
        consumer.addQuad(position, uv, 0f, 0f)

        consumer.assertVertices(arrayOf(Vec2f())) // TODO
    }

    private class GUIConsumer : GUIVertexConsumer {
        override val order = MeshOrder.QUAD
        val vertices: MutableList<Vec2f> = mutableListOf()

        override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) = Broken()
        override fun addVertex(x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) = Broken()

        override fun addCache(cache: GUIMeshCache) = Broken()
        override fun ensureSize(size: Int) = Unit
    }
}
