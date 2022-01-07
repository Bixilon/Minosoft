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

package de.bixilon.minosoft.gui.rendering.gui.elements.items

import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.items.block.BlockItem
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

class ItemElement(
    hudRenderer: HUDRenderer,
    size: Vec2i,
    item: ItemStack?,
) : Element(hudRenderer), Pollable {
    private var count = -1
    private var countText = TextElement(hudRenderer, "", background = false, noBorder = true)

    var item: ItemStack? = item
        set(value) {
            if (field == value) {
                return
            }
            field = value
            apply()
            cacheUpToDate = false
        }

    init {
        _size = size
        forceApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        val item = item ?: return 0
        val size = size
        val countSize = countText.size
        countText.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(size.x, countSize.x), VerticalAlignments.BOTTOM.getOffset(size.y, countSize.y)), z + 1, consumer, options)

        var element: Element? = null

        var color = ChatColors.WHITE
        if (item.item is BlockItem) {
            val defaultState = item.item.block.defaultState
            defaultState.material.color?.let { color = it }
            defaultState.blockModel?.getParticleTexture(KUtil.RANDOM, Vec3i.EMPTY)?.let {
                element = ImageElement(hudRenderer, it)
            }
        }

        (element ?: ColorElement(hudRenderer, _size, color)).render(offset, z + 1, consumer, options)

        // ToDo: Render model
        return TextElement.LAYERS + 1
    }

    override fun poll(): Boolean {
        val item = item ?: return false
        val count = item.count
        if (this.count != count) {
            this.count = count
            return true
        }

        return false
    }

    override fun forceSilentApply() {
        countText.text = when {
            count < 0 -> TextComponent((count < -99).decide({ "-∞" }, { count }), color = ChatColors.RED) // No clue why I do this...
            count == 0 -> TextComponent("0", color = ChatColors.YELLOW)
            count == 1 -> TextComponent("")
            count > ProtocolDefinition.ITEM_STACK_MAX_SIZE -> TextComponent((count > 99).decide({ "∞" }, { count }), color = ChatColors.RED)
            else -> TextComponent(count)
        }

        cacheUpToDate = false
    }

    override fun toString(): String {
        return item.toString()
    }
}
