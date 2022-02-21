/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.popper.item

import de.bixilon.minosoft.data.inventory.stack.ItemStack
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.popper.Popper
import glm_.vec2.Vec2i

class ItemInfoPopper(
    guiRenderer: GUIRenderer,
    position: Vec2i,
    val stack: ItemStack,
) : Popper(guiRenderer, position) {
    private val textElement = TextElement(guiRenderer, "", background = false, parent = this)

    init {
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        textElement.render(offset, consumer, options)
    }

    override fun forceSilentApply() {
        val text = BaseComponent(
            stack.displayName,
            "\n",
            "\n",
            TextComponent(stack.item.item.resourceLocation, color = ChatColors.DARK_GRAY),
        )
        stack._durability?.durability?.let {
            if (it >= 0) {
                text += "\n"
                text += TextComponent("Durability: ${stack._durability?.durability}")
            }
        }
        stack._display?.lore?.let {
            text += "\n"
            for (line in it) {
                text += line
                text += "\n"
            }
        }
        textElement._chatComponent = text
        textElement.forceSilentApply()
        recalculateSize()
        super.forceSilentApply()
    }

    override fun onChildChange(child: Element) {
        recalculateSize()
    }

    private fun recalculateSize() {
        size = textElement.size
    }
}
