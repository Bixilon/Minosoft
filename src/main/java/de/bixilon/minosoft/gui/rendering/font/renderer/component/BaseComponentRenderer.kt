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

import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

object BaseComponentRenderer : ChatComponentRenderer<BaseComponent> {

    override fun render(offset: TextOffset, fontManager: FontManager, properties: TextRenderProperties, info: TextRenderInfo, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, text: BaseComponent): Boolean {
        for (part in text.parts) {
            val filled = ChatComponentRenderer.render(offset, fontManager, properties, info, consumer, options, part)
            if (filled) return true
        }
        return false
    }

    override fun calculatePrimitiveCount(text: BaseComponent): Int {
        var count = 0
        for (part in text.parts) {
            count += ChatComponentRenderer.calculatePrimitiveCount(part)
        }
        return count
    }
}
