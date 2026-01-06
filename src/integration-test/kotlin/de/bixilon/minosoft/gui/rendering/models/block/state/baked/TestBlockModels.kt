/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.models.block.state.render.WorldRenderProps
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.PackedUVArray

object TestBlockModels {
    val TEST1 = TestBlockModel()
    val TEST2 = TestBlockModel()
    val TEST3 = TestBlockModel()


    class TestBlockModel : BlockRender {
        private val positions = FaceVertexData(12)
        private val uv = PackedUVArray()
        private val ao = IntArray(4)

        private val texture = object : ShaderTexture {
            override val shaderId get() = 1
            override val transparency = TextureTransparencies.OPAQUE

            override fun transformUV(uv: Vec2f) = uv

            override fun transformUV(u: Float, v: Float) = PackedUV(u, v)

            override fun transformUV(uv: PackedUV) = uv

            override fun transformU(u: Float) = u

            override fun transformV(v: Float) = v
        }

        override fun render(props: WorldRenderProps, position: BlockPosition, state: BlockState, entity: BlockEntity?, tints: RGBArray?): Boolean {
            props.mesh.addQuad(Vec3f(), positions, uv, texture, 0xFF, ChatColors.WHITE.rgb(), ao)
            return true
        }

        override fun render(consumer: BlockVertexConsumer, state: BlockState, tints: RGBArray?, offset: Vec3f?, light: ByteArray?) {
            consumer.addQuad(Vec3f(), positions, uv, texture, 0xFF, ChatColors.WHITE.rgb(), ao)
        }

        override fun render(offset: Vec3f, consumer: BlockVertexConsumer, stack: ItemStack, tints: RGBArray?) {
            consumer.addQuad(Vec3f(), positions, uv, texture, 0xFF, ChatColors.WHITE.rgb(), ao)
        }
    }
}
