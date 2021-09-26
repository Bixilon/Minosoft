/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.tab

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import glm_.vec2.Vec2i
import java.lang.Integer.max
import java.util.*

class TabListElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    val header = TextElement(hudRenderer, "", background = false, fontAlignment = HorizontalAlignments.CENTER, parent = this)
    val footer = TextElement(hudRenderer, "", background = false, fontAlignment = HorizontalAlignments.CENTER, parent = this)

    private val background = ColorElement(hudRenderer, Vec2i.EMPTY, color = RGBColor(0, 0, 0, 120))

    private var entriesSize = Vec2i.EMPTY
    val entries: MutableMap<UUID, TabListEntryElement> = synchronizedMapOf()
    private var toRender: List<TabListEntryElement> = listOf()

    val pingBarsAtlasElements: Array<HUDAtlasElement> = arrayOf(
        hudRenderer.atlasManager["minecraft:tab_list_ping_0".toResourceLocation()]!!,
        hudRenderer.atlasManager["minecraft:tab_list_ping_1".toResourceLocation()]!!,
        hudRenderer.atlasManager["minecraft:tab_list_ping_2".toResourceLocation()]!!,
        hudRenderer.atlasManager["minecraft:tab_list_ping_3".toResourceLocation()]!!,
        hudRenderer.atlasManager["minecraft:tab_list_ping_4".toResourceLocation()]!!,
        hudRenderer.atlasManager["minecraft:tab_list_ping_5".toResourceLocation()]!!,
    )

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        background.render(Vec2i(offset), z, consumer)
        val size = size

        header.size.let {
            header.onParentChange()
            header.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, it.x), 0), z, consumer)
            offset.y += it.y
        }

        val offsetBefore = Vec2i(offset)
        offset.x += HorizontalAlignments.CENTER.getOffset(size.x, entriesSize.x)

        var columns = toRender.size / ENTRIES_PER_COLUMN
        if (toRender.size % ENTRIES_PER_COLUMN > 0) {
            columns++
        }

        for ((index, entry) in toRender.withIndex()) {
            entry.render(Vec2i(offset), z + 1, consumer)
            offset.y += TabListEntryElement.HEIGHT + ENTRY_VERTICAL_SPACING
            if ((index + 1) % ENTRIES_PER_COLUMN == 0) {
                offset.x += entry.width + ENTRY_HORIZONTAL_SPACING
                offset.y = offsetBefore.y
            }
        }
        offset.x = offsetBefore.x
        offset.y = offsetBefore.y + (columns > 1).decide(ENTRIES_PER_COLUMN, toRender.size) * (TabListEntryElement.HEIGHT + ENTRY_VERTICAL_SPACING)


        footer.size.let {
            footer.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, it.x), 0), z, consumer)
            offset.y += it.y
        }

        return TextElement.LAYERS + 1 // ToDo
    }

    override fun silentApply() {
        val size = Vec2i.EMPTY

        size.y += header.size.y

        val toRender: MutableList<TabListEntryElement> = mutableListOf()

        // ToDo: Sorting isn't working correct:  java.lang.IllegalArgumentException: Comparison method violates its general contract!
        val tabListItems = hudRenderer.connection.tabList.tabListItemsByUUID.toSynchronizedMap().entries.sortedWith { a, b -> a.value.compareTo(b.value) }

        val previousSize = Vec2i(size)
        var columns = tabListItems.size / ENTRIES_PER_COLUMN
        if (tabListItems.size % ENTRIES_PER_COLUMN > 0) {
            columns++
        }

        var column = 0
        val widths = IntArray(columns)
        var currentMaxPrefWidth = 0
        var totalEntriesWidth = 0

        // Check width
        var index = 0
        for ((uuid, item) in tabListItems) {
            val entry = entries.getOrPut(uuid) { TabListEntryElement(hudRenderer, this, item, 0) }
            toRender += entry
            val prefWidth = entry.prefSize

            currentMaxPrefWidth = max(currentMaxPrefWidth, prefWidth.x)
            if ((index + 1) % ENTRIES_PER_COLUMN == 0) {
                widths[column] = currentMaxPrefWidth
                totalEntriesWidth += currentMaxPrefWidth
                currentMaxPrefWidth = 0
                column++
            }
            index++
        }
        if (currentMaxPrefWidth != 0) {
            widths[column] = currentMaxPrefWidth
            totalEntriesWidth += currentMaxPrefWidth
        }
        size.x = max(size.x, totalEntriesWidth)
        size.y += (columns > 1).decide(ENTRIES_PER_COLUMN, toRender.size) * (TabListEntryElement.HEIGHT + ENTRY_VERTICAL_SPACING)

        this.entriesSize = Vec2i(totalEntriesWidth, size.y - previousSize.y)


        // apply width to every cell
        column = 0
        for ((index, entry) in toRender.withIndex()) {
            entry.width = widths[column]
            if ((index + 1) % ENTRIES_PER_COLUMN == 0) {
                column++
            }
        }

        if (columns >= 2) {
            size.x += (columns - 1) * ENTRY_HORIZONTAL_SPACING
        }

        this.toRender = toRender

        size.y += footer.size.y

        size.x = max(max(size.x, header.size.x), footer.size.x)


        this.size = size

        background.size = size
        cacheUpToDate = false
    }

    override fun onParentChange() {
        for (element in toRender) {
            element.onParentChange()
        }
    }


    companion object {
        private const val ENTRIES_PER_COLUMN = 20
        private const val ENTRY_HORIZONTAL_SPACING = 5
        private const val ENTRY_VERTICAL_SPACING = 1
    }
}
