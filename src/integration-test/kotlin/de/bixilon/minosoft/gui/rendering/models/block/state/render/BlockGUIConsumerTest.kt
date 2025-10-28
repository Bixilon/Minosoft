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
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer
import de.bixilon.minosoft.gui.rendering.light.ao.AmbientOcclusionUtil
import de.bixilon.minosoft.gui.rendering.models.raw.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.models.util.CuboidUtil
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.PackedUVArray
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.UnpackedUVArray
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.annotations.Test

@Test(groups = ["models"])
class BlockGUIConsumerTest {

    private fun create(): BlockGUIConsumer {
        val gui = GUIRenderer::class.java.allocate()
        val consumer = GuiConsumer()

        return BlockGUIConsumer(gui, Vec2f(11, 12), consumer, null, ModelDisplay.DEFAULT, Vec2f(45))
    }

    private fun BlockGUIConsumer.assertVertices(expected: Array<Vec2f>) {
        val consumer = this.consumer.unsafeCast<GuiConsumer>()

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
        val uv = UnpackedUVArray(floatArrayOf(0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f)).pack()
        consumer.addQuad(Vec3f.EMPTY, position, uv, DummyTexture(), 0, ChatColors.WHITE.rgb(), AmbientOcclusionUtil.EMPTY)

        consumer.assertVertices(arrayOf(Vec2f())) // TODO
    }

    private class GuiConsumer : GuiVertexConsumer {
        val vertices: MutableList<Vec2f> = mutableListOf()

        override fun addQuad(startX: Float, startY: Float, endX: Float, endY: Float, texture: ShaderTexture, uvStartX: Float, uvStartY: Float, uvEndX: Float, uvEndY: Float, tint: RGBAColor, options: GUIVertexOptions?) = Broken()

        override fun ensureSize(primitives: Int) = Unit
        override fun addQuad(start: Vec2f, end: Vec2f, tint: RGBAColor, options: GUIVertexOptions?) = Broken()

        override fun addChar(start: Vec2f, end: Vec2f, texture: ShaderTexture, uvStart: Vec2f, uvEnd: Vec2f, italic: Boolean, tint: RGBAColor, options: GUIVertexOptions?) = Broken()
        override fun addQuad(positions: FloatArray, uv: PackedUVArray, texture: ShaderTexture, tint: RGBAColor, options: GUIVertexOptions?) = Broken()
    }
}
