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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.text

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasArea
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY

class ContainerText(
    var text: TextElement,
    private var offset: Vec2i = Vec2i.EMPTY,
) {
    fun render(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        text.render(offset + this.offset, consumer, options)
    }


    companion object {
        private val DEFAULT_TEXT_COLOR = ChatColors.DARK_GRAY

        fun of(guiRenderer: GUIRenderer, area: AtlasArea?, text: ChatComponent?): ContainerText? {
            if (area == null || text == null) {
                return null
            }
            text.setFallbackColor(DEFAULT_TEXT_COLOR)
            val textElement = TextElement(guiRenderer, text, background = false, shadow = false)
            textElement.prefMaxSize = area.size

            return ContainerText(textElement, area.start)
        }

        fun createInventoryTitle(guiRenderer: GUIRenderer, area: AtlasArea?): ContainerText? {
            return of(guiRenderer, area, TextComponent("Inventory")) // ToDo: translations
        }
    }
}
