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

import de.bixilon.kotlinglm.GLM.PIf
import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.primitive.IntUtil.toInt
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
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
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
        val rotation = this.properties[BlockProperties.ROTATION]?.toInt() ?: return 0.0f
        return STANDING_ROTATIONS[rotation]
    }

    override fun render(position: BlockPosition, offset: FloatArray, mesh: BlockVertexConsumer, random: Random?, state: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?, entity: BlockEntity?): Boolean {
        state.model?.render(position, offset, mesh, random, state, neighbours, light, tints, entity) // render wood part
        if (entity !is SignBlockEntity) return true

        if (mesh !is ChunkMeshes) return true // TODO

        renderText(state, entity, offset, mesh.textMesh!!, light[SELF_LIGHT_INDEX].toInt())

        return true
    }

    private fun renderText(state: BlockState, sign: SignBlockEntity, offset: FloatArray, mesh: ChunkMesh, light: Int) {
        when (state.block) {
            is StandingSignBlock -> renderStandingText(state.getRotation(), sign, offset, mesh, light)
            is WallSignBlock -> renderWallText(state.getFacing(), sign, offset, mesh, light)
            // TODO: hanging sign
        }
    }

    private fun ChunkMesh.ensureSize(text: SignBlockEntity.SignTextProperties) {
        var primitives = 0
        for (line in text.text) {
            primitives += ChatComponentRenderer.calculatePrimitiveCount(line)
        }
        data.ensureSize(primitives * order.size * ChunkMesh.ChunkMeshStruct.FLOATS_PER_VERTEX)
    }

    private fun renderText(offset: FloatArray, text: SignBlockEntity.SignTextProperties, blockOffset: Vec3, yRotation: Float, mesh: ChunkMesh, light: Int) {
        val textPosition = offset.toVec3() + blockOffset
        val light = if (text.glowing) 0xFF else light
        val rotation = Vec3(0.0f, -yRotation, 0.0f)
        val alignment = context.connection.profiles.block.rendering.entities.sign.fontAlignment
        val properties = if (alignment == TEXT_PROPERTIES.alignment) TEXT_PROPERTIES else TEXT_PROPERTIES.copy(alignment = alignment)

        mesh.ensureSize(text)

        for (line in text.text) {
            ChatComponentRenderer.render3d(context, textPosition, properties, rotation, MAX_SIZE, mesh, line, light)
            textPosition.y -= LINE_HEIGHT
        }
    }

    private fun renderStandingText(rotation: Float, sign: SignBlockEntity, offset: FloatArray, mesh: ChunkMesh, light: Int) {
        val frontOffset = Vec3(STANDING_FRONT_OFFSET).apply { signRotate(rotation) }
        renderText(offset, sign.front, frontOffset, rotation, mesh, light)
        val backOffset = Vec3(STANDING_BACK_OFFSET).apply { signRotate(rotation) }
        renderText(offset, sign.back, backOffset, rotation - 180.0f.rad, mesh, light)
    }

    private fun renderWallText(facing: Directions, sign: SignBlockEntity, offset: FloatArray, mesh: ChunkMesh, light: Int) {
        val rotation = WALL_ROTATIONS[facing.ordinal - Directions.SIDE_OFFSET]
        val blockOffset = Vec3(WALL_OFFSET).apply { signRotate(rotation) }

        renderText(offset, sign.front, blockOffset, rotation, mesh, light)
    }

    private fun Vec3.signRotate(yRotation: Float) {
        this -= 0.5f
        rotateAssign(yRotation, Axes.Y)
        this += 0.5f
    }

    override fun render(gui: GUIRenderer, offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2, stack: ItemStack) = Unit

    companion object {
        private val TEXT_PROPERTIES = TextRenderProperties(scale = 1.35f, allowNewLine = false, shadow = false, fallbackColor = ChatColors.BLACK)
        private const val Z_OFFSET = 0.01f

        const val SIGN_MAX_WIDTH = 90 // 15x the char W. W has a width of 5sp
        val MAX_SIZE = Vec2(SIGN_MAX_WIDTH * TEXT_PROPERTIES.scale, TEXT_PROPERTIES.lineHeight)


        const val SIGN_BOARD_HEIGHT = 8.0f / BLOCK_SIZE
        const val SIGN_BOARD_MARGIN = SIGN_BOARD_HEIGHT / 10.0f
        const val LINE_HEIGHT = (SIGN_BOARD_HEIGHT - (2 * SIGN_BOARD_MARGIN)) / SignBlockEntity.LINES


        const val STANDING_ROTATION_STEPS = 16
        val STANDING_ROTATIONS = FloatArray(STANDING_ROTATION_STEPS) { -it * (2 * PIf) / STANDING_ROTATION_STEPS }
        const val STANDING_BOARD_HEIGHT = 17.5f / BLOCK_SIZE
        const val STANDING_FRONT_DISTANCE = 9.0f / BLOCK_SIZE
        const val STANDING_BACK_DISTANCE = 7.0f / BLOCK_SIZE
        val STANDING_FRONT_OFFSET = Vec3(SIGN_BOARD_MARGIN, STANDING_BOARD_HEIGHT - SIGN_BOARD_MARGIN, STANDING_FRONT_DISTANCE + Z_OFFSET)
        val STANDING_BACK_OFFSET = Vec3(1.0f - SIGN_BOARD_MARGIN, STANDING_BOARD_HEIGHT - SIGN_BOARD_MARGIN, STANDING_BACK_DISTANCE - Z_OFFSET)


        val WALL_ROTATIONS = floatArrayOf(180.0f.rad, 0.0f, 90.0f.rad, 270.0f.rad)
        const val WALL_HEIGHT = 12.5f / BLOCK_SIZE
        const val WALL_DISTANCE = 2.0f / BLOCK_SIZE
        val WALL_OFFSET = Vec3(SIGN_BOARD_MARGIN, WALL_HEIGHT - SIGN_BOARD_MARGIN, WALL_DISTANCE + Z_OFFSET)
    }
}
