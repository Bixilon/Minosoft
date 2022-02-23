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
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.inventory.click.CloneContainerAction
import de.bixilon.minosoft.data.inventory.click.FastMoveContainerAction
import de.bixilon.minosoft.data.inventory.click.SimpleContainerAction
import de.bixilon.minosoft.data.registries.other.containers.Container
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Vec2iBinding
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.elements.item.FloatingItem
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
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
            this.floatingItem = FloatingItem(guiRenderer, it ?: return@observeRendering).apply { show() }
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

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions): Boolean {
        // this is not in items element, because you can also click into "nothing"
        val consumed = super<AbstractLayout>.onMouseAction(position, button, action)
        if (action != MouseActions.PRESS) {
            return consumed
        }

        val shiftDown = guiRenderer.isKeyDown(ModifierKeys.SHIFT)
        val activeElement = activeElement
        if (button == MouseButtons.MIDDLE) {
            if (guiRenderer.connection.player.gamemode != Gamemodes.CREATIVE) {
                return true
            }
            container.invokeAction(CloneContainerAction(activeElement?.slotId ?: return true))
            return true
        }
        if (button == MouseButtons.LEFT || button == MouseButtons.RIGHT) {
            container.invokeAction(if (shiftDown) {
                FastMoveContainerAction(activeElement?.slotId ?: return true)
            } else {
                SimpleContainerAction(activeElement?.slotId, if (button == MouseButtons.LEFT) SimpleContainerAction.ContainerCounts.ALL else SimpleContainerAction.ContainerCounts.PART)
            })
            return true
        }

        return true
    }

    override fun onDragMove(position: Vec2i, absolute: Vec2i, draggable: Dragged): Element? {
        println("Drag: ($draggable) at $position")
        return this
    }

    private data class ItemElementData(
        val element: ItemElement,
        val offset: Vec2i,
    )
}
