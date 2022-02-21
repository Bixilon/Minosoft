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
import de.bixilon.minosoft.data.text.ChatComponent
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
        )
        stack._durability?.durability?.let {
            val max = stack.item.item.maxDurability
            if (it in 0 until max) {
                text += TextComponent(" (${it}/${max})", color = ChatColors.DARK_GRAY)
            }
        }
        stack._display?.lore?.let {
            if (it.isEmpty()) {
                return@let
            }
            for (line in it) {
                text += "\n"
                text += line
            }
        }
        stack._enchanting?.enchantments?.let {
            if (it.isEmpty()) {
                return@let
            }
            text += "\n"
            val language = renderWindow.connection.language
            for ((enchantment, level) in it) {
                text += ChatComponent.of(enchantment, translator = language).apply { applyDefaultColor(ChatColors.BLUE) }
                text += TextComponent(" $level", color = ChatColors.BLUE)
                text += ", "
            }
            if (text.parts.lastOrNull()?.message == ", ") {
                text.parts.removeLast()
            }
        }
        text += "\n\n"
        text += TextComponent(stack.item.item.resourceLocation, color = ChatColors.DARK_GRAY)
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
