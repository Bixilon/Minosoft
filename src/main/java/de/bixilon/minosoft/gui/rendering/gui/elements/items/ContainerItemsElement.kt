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

import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.minosoft.data.registries.other.containers.Container
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Vec2iBinding
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import glm_.vec2.Vec2i
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class ContainerItemsElement(
    guiRenderer: GUIRenderer,
    val container: Container,
    val slots: Int2ObjectOpenHashMap<Vec2iBinding>, // ToDo: Use an array?
) : Element(guiRenderer), Pollable {
    private val itemElements: MutableMap<Int, ItemElementData> = synchronizedMapOf()
    private var revision = -1L

    init {
        silentApply()
        this._size = calculateSize()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        for ((_, data) in itemElements.toSynchronizedMap()) {
            data.element.render(offset + data.offset, consumer, options)
        }
    }

    private fun calculateSize(): Vec2i {
        val size = Vec2i.EMPTY

        for (slot in slots.values) {
            size.x = maxOf(slot.end.x, size.x)
            size.y = maxOf(slot.end.y, size.y)
        }

        return size
    }


    override fun poll(): Boolean {
        val revision = container.revision
        if (this.revision == revision) {
            return false
        }
        this.revision = revision
        return true
    }

    override fun forceSilentApply() {
        var changes = false
        for ((slot, binding) in slots) {
            val item = container[slot]
            val data = itemElements[slot]

            if (data == null) {
                item ?: continue
                val element = ItemElement(
                    guiRenderer = guiRenderer,
                    size = binding.size,
                    item = item,
                )
                itemElements[slot] = ItemElementData(
                    element = element,
                    offset = binding.start,
                )
                // element.parent = this
                changes = true
            } else {
                if (data.element.stack == item) {
                    if (data.element.silentApply()) {
                        changes = true
                    }
                } else {
                    data.element.stack = item
                    changes = true
                }
            }
        }

        if (!changes) {
            return
        }

        cacheUpToDate = false
    }


    private data class ItemElementData(
        val element: ItemElement,
        val offset: Vec2i,
    )
}
