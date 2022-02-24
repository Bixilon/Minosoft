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

import de.bixilon.kutil.watcher.map.MapDataWatcher.Companion.observeMap
import de.bixilon.minosoft.data.registries.other.containers.Container
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Vec2iBinding
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.elements.item.FloatingItem
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isGreater
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isSmaller
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering
import glm_.vec2.Vec2i
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class ContainerItemsElement(
    guiRenderer: GUIRenderer,
    val container: Container,
    val slots: Int2ObjectOpenHashMap<Vec2iBinding>, // ToDo: Use an array?
) : Element(guiRenderer), AbstractLayout<ItemElement> {
    private val itemElements: Int2ObjectOpenHashMap<ItemElementData> = Int2ObjectOpenHashMap()
    private var floatingItem: FloatingItem? = null
    override var activeElement: ItemElement? = null
    override var activeDragElement: ItemElement? = null
    private var update = true
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                cacheUpToDate = false
            }
        }

    init {
        silentApply()

        val size = Vec2i.EMPTY
        for ((slotId, binding) in slots) {
            val item = container[slotId]
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
        this._size = size

        container::slots.observeMap(this) { update = true; }
        container::floatingItem.observeRendering(this) {
            this.floatingItem?.close()
            this.floatingItem = null
            this.floatingItem = FloatingItem(guiRenderer, stack = it ?: return@observeRendering, container = container).apply { show() }
        }
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (update) {
            forceSilentApply()
        }
        for (data in itemElements.values) {
            data.element.render(offset + data.offset, consumer, options)
        }
    }


    override fun forceSilentApply() {
        container.lock.acquire()
        var changes = 0
        for ((slotId, data) in itemElements) {
            val stack = container.slots[slotId]
            if (data.element.stack === stack) {
                continue
            }
            data.element._stack = stack
            changes++
        }
        container.lock.release()

        if (changes > 0) {
            cacheUpToDate = false
        }
        update = false
    }

    override fun getAt(position: Vec2i): Pair<ItemElement, Vec2i>? {
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
        val offset: Vec2i,
    )
}
