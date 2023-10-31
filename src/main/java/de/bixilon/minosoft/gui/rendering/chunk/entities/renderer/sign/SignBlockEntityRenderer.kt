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

package de.bixilon.minosoft.gui.rendering.chunk.entities.renderer.sign

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.block.sign.SignBlockEntity
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.getFacing
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.sign.StandingSignBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.entity.sign.WallSignBlock
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.mesher.SolidSectionMesher.Companion.SELF_LIGHT_INDEX
import de.bixilon.minosoft.gui.rendering.font.renderer.component.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotateAssign
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3
import java.util.*

class SignBlockEntityRenderer(
    val context: RenderContext,
) : BlockRender {

    private fun BlockState.getRotation(): Float {
        if (this !is PropertyBlockState) return 0.0f
        val rotation = this.properties[BlockProperties.ROTATION]?.toFloat() ?: return 0.0f
        return rotation * ROTATION_STEP
    }

    override fun render(position: BlockPosition, offset: FloatArray, mesh: ChunkMeshes, random: Random?, state: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?, entity: BlockEntity?): Boolean {
        state.model?.render(position, offset, mesh, random, state, neighbours, light, tints, entity) // render wood part
        if (entity !is SignBlockEntity) return true
        renderText(state, entity, offset, mesh, light[SELF_LIGHT_INDEX].toInt())

        return true
    }

    private fun renderText(state: BlockState, entity: SignBlockEntity, offset: FloatArray, mesh: ChunkMeshes, light: Int) {
        if (state.block is StandingSignBlock) {
            renderStandingText(state.getRotation(), entity, offset, mesh, light)
        } else if (state.block is WallSignBlock) {
            renderWallText(state.getFacing(), entity, offset, mesh, light)
        }
    }

    private fun renderText(offset: FloatArray, entity: SignBlockEntity, rotationVector: Vec3, yRotation: Float, mesh: ChunkMesh, light: Int) {
        val textPosition = offset.toVec3() + rotationVector

        var primitives = 0
        for (line in entity.lines) {
            primitives += ChatComponentRenderer.calculatePrimitiveCount(line)
        }
        mesh.data.ensureSize(primitives * mesh.order.size * ChunkMesh.ChunkMeshStruct.FLOATS_PER_VERTEX)

        val alignment = context.connection.profiles.block.rendering.entities.sign.fontAlignment

        val properties = if (alignment == TEXT_PROPERTIES.alignment) TEXT_PROPERTIES else TEXT_PROPERTIES.copy(alignment = alignment)

        for (line in entity.lines) {
            ChatComponentRenderer.render3dFlat(context, textPosition, properties, Vec3(0.0f, -yRotation, 0.0f), MAX_SIZE, mesh, line, light)
            textPosition.y -= 0.11f
        }
    }

    private fun renderStandingText(rotation: Float, entity: SignBlockEntity, offset: FloatArray, mesh: ChunkMeshes, light: Int) {
        val rotationVector = Vec3(X_OFFSET, 17.5f / BLOCK_SIZE - Y_OFFSET, 9.0f / BLOCK_SIZE + Z_OFFSET)
        rotationVector.signRotate(rotation.rad)
        renderText(offset, entity, rotationVector, rotation, mesh.textMesh!!, light)
    }

    private fun renderWallText(facing: Directions, entity: SignBlockEntity, position: FloatArray, mesh: ChunkMeshes, light: Int) {
        val yRotation = ROTATIONS[facing.ordinal - Directions.SIDE_OFFSET]
        val rotationVector = Vec3(X_OFFSET, 12.5f / BLOCK_SIZE - Y_OFFSET, 2.0f / BLOCK_SIZE + Z_OFFSET)
        rotationVector.signRotate(yRotation.rad)
        renderText(position, entity, rotationVector, yRotation, mesh.textMesh!!, light)
    }

    private fun Vec3.signRotate(yRotation: Float) {
        this -= 0.5f
        rotateAssign(yRotation, Axes.Y)
        this += 0.5f
    }

    override fun render(gui: GUIRenderer, offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2, stack: ItemStack) = Unit

    companion object {
        private val TEXT_PROPERTIES = TextRenderProperties(scale = 1.35f, allowNewLine = false, shadow = false, fallbackColor = ChatColors.BLACK)
        private const val PIXEL_SCALE = 1.0f / ChatComponentRenderer.TEXT_BLOCK_RESOLUTION
        private const val Z_OFFSET = 0.01f
        private const val X_OFFSET = PIXEL_SCALE * 6
        private const val Y_OFFSET = 0.04f
        private const val ROTATION_STEP = 22.5f
        const val SIGN_MAX_WIDTH = 90 // 15x the char W. W has a width of 5sp
        val MAX_SIZE = Vec2(SIGN_MAX_WIDTH * TEXT_PROPERTIES.scale, TEXT_PROPERTIES.lineHeight)


        private val ROTATIONS = floatArrayOf(180.0f, 0.0f, 90.0f, 270.0f)
    }
}
