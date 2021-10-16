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

import de.bixilon.minosoft.data.player.tab.TabListItem
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import glm_.vec2.Vec2i
import java.lang.Integer.max

class TabListEntryElement(
    hudRenderer: HUDRenderer,
    val tabList: TabListElement,
    val item: TabListItem,
    width: Int,
) : Element(hudRenderer) {
    init {
        _parent = tabList
    }

    // ToDo: Skin
    private val background: ColorElement

    private val nameElement = TextElement(hudRenderer, "", background = false, parent = this)
    private lateinit var pingElement: ImageElement

    private var displayName: ChatComponent = ChatComponent.EMPTY
    private var ping = -1

    override var prefSize: Vec2i = Vec2i.EMPTY
    override var prefMaxSize: Vec2i
        get() = Vec2i(width, HEIGHT)
        set(value) {}
    override var size: Vec2i
        get() = maxSize
        set(value) {}

    var width: Int = width
        set(value) {
            if (value == field) {
                return
            }
            field = value
            forceApply()
        }

    init {
        background = ColorElement(hudRenderer, size, RGBColor(120, 120, 120, 130))
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        background.render(Vec2i(offset), z, consumer)
        nameElement.render(Vec2i(offset), z, consumer)
        pingElement.render(offset + Vec2i(HorizontalAlignments.RIGHT.getOffset(maxSize.x, pingElement.size.x + PADDING), PADDING), z + 1, consumer)

        return TextElement.LAYERS
    }

    override fun forceSilentApply() {
        // ToDo (Performance): If something changed, should we just prepare the changed
        pingElement = ImageElement(hudRenderer, tabList.pingBarsAtlasElements[when {
            ping < 0 -> 0
            ping < 150 -> 5
            ping < 300 -> 4
            ping < 600 -> 3
            ping < 1000 -> 2
            else -> 1
        }])
        nameElement.prefMaxSize = Vec2i(max(0, maxSize.x - pingElement.size.x), HEIGHT)

        nameElement.text = displayName

        this.prefSize = Vec2i((PADDING * 2) + nameElement.prefSize.x + INNER_MARGIN + pingElement.prefSize.x, HEIGHT)
        background.size = size
        cacheUpToDate = false
    }

    override fun silentApply(): Boolean {
        val ping = item.ping
        val displayName = item.tabDisplayName

        if (this.ping == ping && this.displayName == displayName) {
            return false
        }

        this.ping = ping
        this.displayName = displayName

        forceSilentApply()
        return true
    }


    companion object {
        const val HEIGHT = 10
        const val INNER_MARGIN = 5
        const val PADDING = 1
    }
}
