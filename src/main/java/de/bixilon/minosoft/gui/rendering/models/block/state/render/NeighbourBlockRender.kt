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

package de.bixilon.minosoft.gui.rendering.models.block.state.render

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.models.raw.display.DisplayPositions
import de.bixilon.minosoft.gui.rendering.models.raw.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import java.util.*

interface NeighbourBlockRender : BlockRender {
    val default: BlockRender?


    override fun render(gui: GUIRenderer, offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?, size: Vec2f, stack: ItemStack, tints: RGBArray?) {
        default?.render(gui, offset, consumer, options, size, stack, tints)
    }

    override fun render(consumer: BlockVertexConsumer, state: BlockState, tints: RGBArray?, offset: Vec3f?, light: ByteArray?) {
        default?.render(consumer, state, tints, offset, light)
    }

    override fun render(offset: Vec3f, consumer: BlockVertexConsumer, stack: ItemStack, tints: RGBArray?) {
        default?.render(offset, consumer, stack, tints)
    }

    override fun getProperties(direction: Directions): SideProperties? {
        return default?.getProperties(direction) // both models should have the same properties
    }

    override fun getDisplay(position: DisplayPositions): ModelDisplay? {
        return default?.getDisplay(position)
    }

    override fun getParticleTexture(random: Random?, position: BlockPosition): Texture? {
        return default?.getParticleTexture(random, position)
    }
}
