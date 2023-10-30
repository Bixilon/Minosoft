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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.FaceCulling
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import java.util.*

class BakedModel(
    val faces: Array<Array<BakedFace>>,
    val properties: Array<SideProperties?>,
    val particle: Texture?,
) : BlockRender {

    init {
        if (faces.size != Directions.SIZE) throw IllegalArgumentException()
        if (properties.size != Directions.SIZE) throw IllegalArgumentException()
    }

    override fun getProperties(direction: Directions) = properties[direction.ordinal]

    override fun getParticleTexture(random: Random?, position: Vec3i) = particle

    override fun render(position: BlockPosition, offset: FloatArray, mesh: ChunkMeshes, random: Random?, state: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?): Boolean {
        var rendered = false

        for ((directionIndex, faces) in faces.withIndex()) {
            val neighbour = neighbours[directionIndex]
            val direction = Directions.VALUES[directionIndex].inverted

            for (face in faces) {
                if (FaceCulling.canCull(state, face.properties, direction, neighbour)) {
                    continue
                }
                face.render(offset, mesh, light, tints)

                rendered = true
            }
        }

        return rendered
    }

    override fun render(gui: GUIRenderer, offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2, stack: ItemStack) {
        val texture = particle ?: return
        ImageElement(gui, texture, size = size).render(offset, consumer, options)

        // TODO: create projection/view matrix and look at the edge of the block. Then map the resulting 3d coordinates to 2d space
    }
}
