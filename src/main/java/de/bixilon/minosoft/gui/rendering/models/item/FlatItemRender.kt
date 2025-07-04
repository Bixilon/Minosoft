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

package de.bixilon.minosoft.gui.rendering.models.item

import glm_.vec2.Vec2
import glm_.vec3.Vec3
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.block.element.face.FaceUV
import de.bixilon.minosoft.gui.rendering.models.util.CuboidUtil
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

class FlatItemRender(
    val layers: Array<Texture>,
    override val particle: Texture?,
) : ItemRender {

    override fun render(gui: GUIRenderer, offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2, stack: ItemStack, tints: RGBArray?) {
        for ((index, layer) in layers.withIndex()) {
            val tint = tints?.get(index)?.rgba() ?: ChatColors.WHITE
            ImageElement(gui, layer, size = size, tint = tint).render(offset, consumer, options)
        }
    }

    override fun render(mesh: BlockVertexConsumer, stack: ItemStack, tints: RGBArray?) {
        for ((index, layer) in layers.withIndex()) {
            mesh.addQuad(POSITIONS, UV, layer.shaderId.buffer(), (tints?.get(index) ?: Colors.WHITE_RGB).rgb.buffer())
        }
        // TODO: items have depth
        // TODO: light, ...
    }


    private companion object {
        val POSITIONS = CuboidUtil.positions(Directions.NORTH, Vec3(0.3f, 0.0f, 0.5f), Vec3(0.6f, 0.3f, 0.5f))
        val UV = FaceUV(Vec2(0.0f), Vec2(1.0f)).toArray(Directions.NORTH, 2)
    }
}
