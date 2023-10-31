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

package de.bixilon.minosoft.gui.rendering.models.block.state.builder

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import java.util.*

class BuiltModel(
    // TODO: this fucks up the rendering order, because it mixes static models with dynamic ones
    val model: BakedModel,
    val dynamic: Array<BlockRender>,
) : BlockRender {

    override fun render(position: BlockPosition, offset: FloatArray, mesh: ChunkMeshes, random: Random?, state: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?, entity: BlockEntity?): Boolean {
        var rendered = model.render(position, offset, mesh, random, state, neighbours, light, tints, entity)

        for (dynamic in this.dynamic) {
            if (dynamic.render(position, offset, mesh, random, state, neighbours, light, tints, entity)) {
                rendered = true
            }
        }

        return rendered
    }

    override fun render(gui: GUIRenderer, offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2, stack: ItemStack) {
        model.render(gui, offset, consumer, options, size, stack)

        for (dynamic in this.dynamic) {
            dynamic.render(gui, offset, consumer, options, size, stack)
        }
    }

    override fun getProperties(direction: Directions): SideProperties? {
        return model.getProperties(direction) // TODO: dynamic?
    }

    override fun getParticleTexture(random: Random?, position: Vec3i): Texture? {
        return model.getParticleTexture(random, position)
    }
}
