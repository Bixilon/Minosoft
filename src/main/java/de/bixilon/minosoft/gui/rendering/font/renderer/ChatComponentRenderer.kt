/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.font.renderer

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateDegreesAssign
import de.bixilon.minosoft.gui.rendering.world.mesh.SingleWorldMesh
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh

interface ChatComponentRenderer<T : ChatComponent> {

    /**
     * Returns true if the text exceeded the maximum size
     */
    fun render(initialOffset: Vec2i, offset: Vec2i, size: Vec2i, element: Element, renderWindow: RenderWindow, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, renderInfo: TextRenderInfo, text: T): Boolean

    fun render3dFlat(renderWindow: RenderWindow, matrix: Mat4, scale: Float, mesh: SingleWorldMesh, text: T, light: Int)

    fun calculatePrimitiveCount(text: T): Int

    companion object : ChatComponentRenderer<ChatComponent> {
        const val TEXT_BLOCK_RESOLUTION = 128

        override fun render(initialOffset: Vec2i, offset: Vec2i, size: Vec2i, element: Element, renderWindow: RenderWindow, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, renderInfo: TextRenderInfo, text: ChatComponent): Boolean {
            return when (text) {
                is BaseComponent -> BaseComponentRenderer.render(initialOffset, offset, size, element, renderWindow, consumer, options, renderInfo, text)
                is TextComponent -> TextComponentRenderer.render(initialOffset, offset, size, element, renderWindow, consumer, options, renderInfo, text)
                else -> TODO("Don't know how to render ${text::class.java}")
            }
        }

        override fun render3dFlat(renderWindow: RenderWindow, matrix: Mat4, scale: Float, mesh: SingleWorldMesh, text: ChatComponent, light: Int) {
            when (text) {
                is BaseComponent -> BaseComponentRenderer.render3dFlat(renderWindow, matrix, scale, mesh, text, light)
                is TextComponent -> TextComponentRenderer.render3dFlat(renderWindow, matrix, scale, mesh, text, light)
                else -> TODO("Don't know how to render ${text::class.java}")
            }
        }

        fun render3dFlat(renderWindow: RenderWindow, position: Vec3, scale: Float, rotation: Vec3, mesh: WorldMesh, text: ChatComponent, light: Int) {
            val matrix = Mat4()
                .translateAssign(position)
                .rotateDegreesAssign(rotation)
                .translateAssign(Vec3(0, 0, -1))

            val textMesh = mesh.textMesh!!
            val primitives = calculatePrimitiveCount(text)
            textMesh.data.ensureSize(primitives * textMesh.order.size * SingleWorldMesh.SectionArrayMeshStruct.FLOATS_PER_VERTEX)

            render3dFlat(renderWindow, matrix, scale, textMesh, text, light)
        }

        override fun calculatePrimitiveCount(text: ChatComponent): Int {
            return when (text) {
                is BaseComponent -> BaseComponentRenderer.calculatePrimitiveCount(text)
                is TextComponent -> TextComponentRenderer.calculatePrimitiveCount(text)
                else -> TODO("Don't know how to render ${text::class.java}")
            }
        }
    }
}
