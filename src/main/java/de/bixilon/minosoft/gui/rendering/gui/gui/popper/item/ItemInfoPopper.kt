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

package de.bixilon.minosoft.gui.rendering.gui.gui.popper.item

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.popper.MouseTrackedPopper
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

class ItemInfoPopper(
    guiRenderer: GUIRenderer,
    position: Vec2,
    val stack: ItemStack,
) : MouseTrackedPopper(guiRenderer, position) {
    private val textElement = TextElement(guiRenderer, "", background = null, parent = this)

    init {
        update()
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        textElement.render(offset, consumer, options)
    }

    override fun update() {
        val text = BaseComponent(
            stack.displayName,
        )
        stack._durability?.durability?.let {
            if (stack.item.item !is DurableItem) return@let
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
            val language = context.connection.language
            for ((enchantment, level) in it) {
                text += ChatComponent.of(enchantment, translator = language).apply { this.setFallbackColor(ChatColors.BLUE) }
                text += TextComponent(" $level", color = ChatColors.BLUE)
                text += ", "
            }
            if (text.parts.lastOrNull()?.message == ", ") {
                text.parts.removeLast()
            }
        }
        text += "\n\n"
        text += TextComponent(stack.item.item.identifier, color = ChatColors.DARK_GRAY)
        textElement.chatComponent = text
        textElement.update()
        recalculateSize()
        super.update()
    }

    private fun recalculateSize() {
        size = textElement.size
    }
}
