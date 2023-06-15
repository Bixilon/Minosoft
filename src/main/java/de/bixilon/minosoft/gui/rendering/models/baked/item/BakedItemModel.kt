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

package de.bixilon.minosoft.gui.rendering.models.baked.item

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions.Companion.copy
import de.bixilon.minosoft.gui.rendering.models.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture

@Deprecated("ToDo")
class BakedItemModel(
    val texture: AbstractTexture?,
) : BakedModel {

    private fun renderDurability(guiRenderer: GUIRenderer, offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2, stack: ItemStack) {
        if (stack.item.item !is DurableItem) return
        val durability = stack._durability?._durability
        val maxDurability = stack.item.item.maxDurability
        if (durability == null || durability < 0 || durability == stack.item.item.maxDurability) {
            return
        }

        val percent = (durability / maxDurability.toFloat())
        val width = size.x
        val fillWidth = width * percent
        val whiteTexture = guiRenderer.context.textureManager.whiteTexture

        consumer.addQuad(offset + Vec2i(2, size.y - 3), offset + Vec2i(size.x, size.y - 1), whiteTexture, tint = ChatColors.BLACK, options = options)

        val color = RGBColor(1.0f - percent, percent, 0.0f) // ToDo: Color transition, something like https://gist.github.com/mlocati/7210513
        consumer.addQuad(offset + Vec2i(1, size.y - 3), offset + Vec2i(fillWidth - 1, size.y - 2), whiteTexture, tint = color, options = options)
    }

    fun render2d(guiRenderer: GUIRenderer, offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?, size: Vec2, stack: ItemStack) {
        val texture = texture ?: return
        val tint = guiRenderer.context.tintManager.getItemTint(stack)
        consumer.addQuad(offset, offset + size, texture, tint = tint?.let { it[0].asRGBColor() } ?: ChatColors.WHITE, options = options) // TODO: multiple tint layers

        renderDurability(guiRenderer, offset, consumer, options, size, stack)

        if (stack._enchanting?.enchantments?.isNotEmpty() == true) {
            consumer.addQuad(offset, offset + size, guiRenderer.context.textureManager.whiteTexture, tint = ChatColors.BLUE, options = options.copy(alpha = 0.5f))
        }
    }
}
