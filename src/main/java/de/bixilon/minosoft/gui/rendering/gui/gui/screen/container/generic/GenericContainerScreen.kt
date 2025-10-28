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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.generic

import de.bixilon.kmath.vec.vec2.f.MVec2f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.container.types.generic.GenericContainer
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasArea
import de.bixilon.minosoft.gui.rendering.gui.atlas.AtlasElement
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.items.ContainerItemsElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.ContainerGUIFactory
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.ContainerScreen
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.container.text.ContainerText
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.isSmaller
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlin.reflect.KClass

open class GenericContainerScreen(
    guiRenderer: GUIRenderer,
    container: GenericContainer,
) : ContainerScreen<GenericContainer>(
    guiRenderer,
    container,
    Int2ObjectOpenHashMap(),
) {
    private val atlas = guiRenderer.atlas[ATLAS]
    private val headerAtlas = atlas["header"]
    private val slotRowAtlas = atlas["row"]
    private val footerAtlas = atlas["footer"]
    private val header = AtlasImageElement(guiRenderer, headerAtlas)
    private val slotRow = AtlasImageElement(guiRenderer, slotRowAtlas)
    private val footer = AtlasImageElement(guiRenderer, footerAtlas)

    override val containerElement = ContainerItemsElement(guiRenderer, container, calculateSlots()).apply { parent = this@GenericContainerScreen }
    override val customRenderer: Boolean get() = true
    private val containerSize = Vec2f(maxOf(header.size.x, slotRow.size.x, footer.size.x), header.size.y + slotRow.size.y * container.rows + footer.size.y)

    private val title = ContainerText.of(guiRenderer, headerAtlas?.areas?.get("text"), container.title)
    private val inventoryTitle = ContainerText.createInventoryTitle(guiRenderer, footerAtlas?.areas?.get("text"))


    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
        val centerOffset = MVec2f((size - containerSize) / 2)
        val initialOffset = centerOffset.final()

        super.forceRender(centerOffset.unsafe, consumer, options)

        header.render(centerOffset.unsafe, consumer, options)
        if (container.title != null) {
            title?.render(centerOffset.unsafe, consumer, options)
        }
        centerOffset.y += header.size.y
        for (i in 0 until container.rows) {
            slotRow.render(centerOffset.unsafe, consumer, options)
            centerOffset.y += slotRow.size.y
        }
        footer.render(centerOffset.unsafe, consumer, options)
        inventoryTitle?.render(centerOffset.unsafe, consumer, options)

        forceRenderContainerScreen(initialOffset, consumer, options)
    }

    override fun getAt(position: Vec2f): Pair<Element, Vec2f>? {
        val centerOffset = (size - containerSize) / 2
        if (position isSmaller centerOffset) {
            return null
        }
        return super.getAt(position - centerOffset)
    }

    private fun calculateSlots(): Int2ObjectOpenHashMap<AtlasArea> {
        val slots: Int2ObjectOpenHashMap<AtlasArea> = Int2ObjectOpenHashMap()
        var slotOffset = 0
        val offset = MVec2f(0, 0)

        fun pushElement(atlasElement: AtlasElement) {
            if (atlasElement.slots != null) {
                for ((slotId, slot) in atlasElement.slots) {
                    slots[slotId + slotOffset] = AtlasArea(slot.start + offset, slot.end + offset)
                }
                slotOffset += atlasElement.slots.size
            }
            offset.y += atlasElement.size.y
        }
        headerAtlas?.let { pushElement(it) }

        slotRowAtlas?.let {
            for (row in 0 until container.rows) {
                pushElement(it)
            }
        }
        footerAtlas?.let { pushElement(it) }
        return slots
    }

    companion object : ContainerGUIFactory<GenericContainerScreen, GenericContainer> {
        private val ATLAS = minecraft("container/generic")
        override val clazz: KClass<GenericContainer> = GenericContainer::class

        override fun register(gui: GUIRenderer) {
            gui.atlas.load(ATLAS)
        }

        override fun build(gui: GUIRenderer, container: GenericContainer): GenericContainerScreen {
            return GenericContainerScreen(gui, container)
        }
    }
}
