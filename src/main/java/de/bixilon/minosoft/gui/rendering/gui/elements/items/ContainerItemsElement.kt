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

package de.bixilon.minosoft.gui.rendering.gui.elements.items

import de.bixilon.kmath.vec.vec2.f.MVec2f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.map.MapObserver.Companion.observeMap
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasArea
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.elements.item.FloatingItem
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.isGreater
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.isSmaller
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class ContainerItemsElement(
    guiRenderer: GUIRenderer,
    val container: Container,
    val slots: Int2ObjectMap<AtlasArea>, // ToDo: Use an array?
) : Element(guiRenderer), AbstractLayout<ItemElement> {
    private val itemElements: Int2ObjectOpenHashMap<ItemElementData> = Int2ObjectOpenHashMap()
    private var floatingItem: FloatingItem? = null
    override var activeElement: ItemElement? = null
    override var activeDragElement: ItemElement? = null

    init {
        silentApply()

        val size = MVec2f.EMPTY
        for ((slotId, binding) in slots) {
            val item = container.items[slotId]
            itemElements[slotId] = ItemElementData(
                element = ItemElement(
                    guiRenderer = guiRenderer,
                    size = binding.size,
                    item = item,
                    slotId = slotId,
                    itemsElement = this,
                ),
                offset = binding.start,
            )
            size.x = maxOf(binding.end.x, size.x)
            size.y = maxOf(binding.end.y, size.y)
        }
        this._size = size.unsafe

        container.items::slots.observeMap(this) {
            for ((slot, _) in it.removes) {
                itemElements[slot]?.element?.stack = null
            }
            for ((slot, stack) in it.adds) {
                itemElements[slot]?.element?.stack = stack
            }
        }
        container::floating.observe(this) {
            this.floatingItem?.close()
            this.floatingItem = it?.let { FloatingItem(guiRenderer, stack = it, container = container).apply { show() } }
        }
    }

    override fun forceRender(offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        for (data in itemElements.values) {
            data.element.render(offset + data.offset, consumer, options)
        }
    }


    override fun forceSilentApply() {
    }

    override fun getAt(position: Vec2f): Pair<ItemElement, Vec2f>? {
        for (item in itemElements.values) {
            if (position isSmaller item.offset) {
                continue
            }
            val innerOffset = position - item.offset
            if (innerOffset isGreater item.element.size) {
                continue
            }
            return Pair(item.element, innerOffset)
        }
        return null
    }

    private data class ItemElementData(
        val element: ItemElement,
        val offset: Vec2f,
    )
}
