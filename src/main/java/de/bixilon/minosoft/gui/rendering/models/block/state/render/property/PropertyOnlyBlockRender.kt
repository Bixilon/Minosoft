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

package de.bixilon.minosoft.gui.rendering.models.block.state.render.property

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.models.block.state.render.WorldRenderProps

interface PropertyOnlyBlockRender : BlockRender {
    override fun render(props: WorldRenderProps, position: BlockPosition, state: BlockState, entity: BlockEntity?, tints: RGBArray?) = false

    override fun render(gui: GUIRenderer, offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2f, stack: ItemStack, tints: RGBArray?) = Unit
    override fun render(mesh: BlockVertexConsumer, state: BlockState, tints: RGBArray?) = Unit
    override fun render(mesh: BlockVertexConsumer, stack: ItemStack, tints: RGBArray?) = Unit
}
