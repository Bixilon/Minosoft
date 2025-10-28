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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.light.ao.AmbientOcclusionUtil
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.FaceCulling
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.models.block.state.render.WorldRenderProps
import de.bixilon.minosoft.gui.rendering.models.raw.display.DisplayPositions
import de.bixilon.minosoft.gui.rendering.models.raw.display.ModelDisplay
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import java.util.*

class BakedModel(
    val faces: Array<Array<BakedFace>>,
    val properties: Array<SideProperties?>,
    val display: Map<DisplayPositions, ModelDisplay>?,
    override val particle: Texture?,
) : BlockRender {

    init {
        if (faces.size != Directions.SIZE) throw IllegalArgumentException()
        if (properties.size != Directions.SIZE) throw IllegalArgumentException()
    }

    override fun getProperties(direction: Directions) = properties[direction.ordinal]

    override fun getParticleTexture(random: Random?, position: BlockPosition) = particle

    override fun render(props: WorldRenderProps, position: BlockPosition, state: BlockState, entity: BlockEntity?, tints: RGBArray?): Boolean {
        var rendered = false

        val offset = props.offset
        val mesh = props.mesh
        val light = props.light
        val ao = props.ao
        val neighbours = props.neighbours


        for ((directionIndex, faces) in faces.withIndex()) {
            val neighbour = neighbours[directionIndex]
            val direction = Directions.VALUES[directionIndex]
            val inverted = direction.inverted


            for (face in faces) {
                if (FaceCulling.canCull(state, face.properties, inverted, neighbour)) {
                    continue
                }

                var aoRaw = AmbientOcclusionUtil.EMPTY

                if (ao != null && face.properties != null) {
                    aoRaw = ao.apply(direction, position.inSectionPosition)
                }

                face.render(offset, mesh, light, tints, aoRaw)

                rendered = true
            }
        }

        return rendered
    }

    private fun render(offset: Vec3f, mesh: BlockVertexConsumer, tints: RGBArray?) {
        for (side in this.faces) {
            for (face in side) {
                face.render(offset, mesh, tints)
            }
        }
    }

    override fun render(consumer: BlockVertexConsumer, state: BlockState, tints: RGBArray?) = render(Vec3f.EMPTY, consumer, tints)
    override fun render(offset: Vec3f, consumer: BlockVertexConsumer, stack: ItemStack, tints: RGBArray?) = render(offset, consumer, tints)

    override fun getDisplay(position: DisplayPositions): ModelDisplay? {
        return this.display?.get(position)
    }
}
