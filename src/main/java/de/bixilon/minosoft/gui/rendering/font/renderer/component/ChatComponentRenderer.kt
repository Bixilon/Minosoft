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

package de.bixilon.minosoft.gui.rendering.font.renderer.component

import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.EmptyComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshBuilder
import de.bixilon.minosoft.gui.rendering.font.WorldGUIConsumer
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

interface ChatComponentRenderer<T : ChatComponent> {

    /**
     * Returns true if the text exceeded the maximum size
     */
    fun render(offset: TextOffset, fontManager: FontManager, properties: TextRenderProperties, info: TextRenderInfo, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, text: T): Boolean

    fun calculatePrimitiveCount(text: T): Int

    companion object : ChatComponentRenderer<ChatComponent> {
        const val TEXT_BLOCK_RESOLUTION = 128

        override fun render(offset: TextOffset, fontManager: FontManager, properties: TextRenderProperties, info: TextRenderInfo, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, text: ChatComponent) = when (text) {
            is BaseComponent -> BaseComponentRenderer.render(offset, fontManager, properties, info, consumer, options, text)
            is TextComponent -> TextComponentRenderer.render(offset, fontManager, properties, info, consumer, options, text)
            is EmptyComponent -> false
            else -> TODO("Don't know how to render ${text::class.java}")
        }

        fun render3d(context: RenderContext, position: Vec3f, properties: TextRenderProperties, rotation: Vec3f, maxSize: Vec2f, mesh: ChunkMeshBuilder, text: ChatComponent, light: Int): TextRenderInfo {
            val matrix = MMat4f().apply {
                translateAssign(position)
                rotateRadAssign(rotation)
                translateZAssign(-1.0f)
            }

            val primitives = calculatePrimitiveCount(text)
            mesh.ensureSize(primitives)

            val consumer = WorldGUIConsumer(mesh, matrix.unsafe, light)
            return render3d(context, properties, maxSize, consumer, text, null)
        }

        fun render3d(context: RenderContext, properties: TextRenderProperties, maxSize: Vec2f, mesh: GUIVertexConsumer, text: ChatComponent, background: RGBAColor? = RenderConstants.TEXT_BACKGROUND_COLOR): TextRenderInfo {
            val primitives = calculatePrimitiveCount(text)
            mesh.ensureSize(primitives)

            val info = TextRenderInfo(maxSize)
            render(TextOffset(), context.font, properties, info, null, null, text)
            info.rewind()
            if (background != null) {
                mesh.addQuad(Vec2f(-1, 0), info.size.unsafe + Vec2f(1f, 0f), background, null)
            }
            val size = info.size.x
            info.size.x = maxSize.x // this allows font aligning

            render(TextOffset(), context.font, properties, info, mesh, null, text)
            info.size.x = size

            return info
        }

        override fun calculatePrimitiveCount(text: ChatComponent) = when (text) {
            is BaseComponent -> BaseComponentRenderer.calculatePrimitiveCount(text)
            is TextComponent -> TextComponentRenderer.calculatePrimitiveCount(text)
            is EmptyComponent -> 0
            else -> TODO("Don't know how to render ${text::class.java}")
        }
    }
}
