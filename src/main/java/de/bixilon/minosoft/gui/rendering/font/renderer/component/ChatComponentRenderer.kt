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

package de.bixilon.minosoft.gui.rendering.font.renderer.component

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.SingleWorldMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.font.WorldGUIConsumer
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateDegreesAssign

interface ChatComponentRenderer<T : ChatComponent> {

    /**
     * Returns true if the text exceeded the maximum size
     */
    fun render(offset: TextOffset, fontManager: FontManager, properties: TextRenderProperties, info: TextRenderInfo, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, text: T): Boolean

    fun calculatePrimitiveCount(text: T): Int

    companion object : ChatComponentRenderer<ChatComponent> {
        const val TEXT_BLOCK_RESOLUTION = 128

        override fun render(offset: TextOffset, fontManager: FontManager, properties: TextRenderProperties, info: TextRenderInfo, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, text: ChatComponent): Boolean {
            return when (text) {
                is BaseComponent -> BaseComponentRenderer.render(offset, fontManager, properties, info, consumer, options, text)
                is TextComponent -> TextComponentRenderer.render(offset, fontManager, properties, info, consumer, options, text)
                is EmptyComponent -> return false
                else -> TODO("Don't know how to render ${text::class.java}")
            }
        }

        fun render3dFlat(context: RenderContext, position: Vec3, properties: TextRenderProperties, rotation: Vec3, maxSize: Vec2, mesh: WorldMesh, text: ChatComponent, light: Int): TextRenderInfo {
            val matrix = Mat4()
                .translateAssign(position)
                .rotateDegreesAssign(rotation)
                .translateAssign(Vec3(0, 0, -1))

            val textMesh = mesh.textMesh!!
            val primitives = calculatePrimitiveCount(text)
            textMesh.data.ensureSize(primitives * textMesh.order.size * SingleWorldMesh.WorldMeshStruct.FLOATS_PER_VERTEX)

            val consumer = WorldGUIConsumer(textMesh, matrix, light)

            val info = TextRenderInfo(maxSize)
            render(TextOffset(), context.font, properties, info, null, null, text)
            info.rewind()
            info.size.x = maxSize.x // this allows font aligning
            render(TextOffset(), context.font, properties, info, consumer, null, text)

            return info
        }

        override fun calculatePrimitiveCount(text: ChatComponent): Int {
            return when (text) {
                is BaseComponent -> BaseComponentRenderer.calculatePrimitiveCount(text)
                is TextComponent -> TextComponentRenderer.calculatePrimitiveCount(text)
                is EmptyComponent -> 0
                else -> TODO("Don't know how to render ${text::class.java}")
            }
        }
    }
}
