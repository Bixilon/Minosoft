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
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.ElementAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.ElementAlignments.Companion.getOffset
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
        parent = tabList
    }

    // ToDo: Skin
    private val background: ColorElement

    private val nameElement = TextElement(hudRenderer, "", background = false, parent = this)
    private lateinit var pingElement: ImageElement

    private var lastDisplayName: ChatComponent? = null
    private var lastPing = -1

    override var prefSize: Vec2i = Vec2i.EMPTY
    override var prefMaxSize: Vec2i
        get() = Vec2i(width, HEIGHT)
        set(value) {}
    override var size: Vec2i
        get() = maxSize
        set(value) {}

    private var forcePrepare = true

    var width: Int = width
        set(value) {
            if (value == field) {
                return
            }
            field = value
            forcePrepare = true
            apply()
        }

    init {
        background = ColorElement(hudRenderer, size, RGBColor(80, 80, 80, 130))
        silentApply()
    }

    override fun render(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        background.render(Vec2i(offset), z, consumer)
        nameElement.render(Vec2i(offset), z, consumer)
        pingElement.render(offset + Vec2i(ElementAlignments.RIGHT.getOffset(maxSize.x, pingElement.size.x + PADDING), PADDING), z + 1, consumer)

        return TextElement.LAYERS
    }

    override fun silentApply() {
        val ping = item.ping

        if (forcePrepare || ping != lastPing) {
            pingElement = ImageElement(hudRenderer, tabList.pingBarsAtlasElements[when {
                ping < 0 -> 0
                ping < 150 -> 5
                ping < 300 -> 4
                ping < 600 -> 3
                ping < 1000 -> 2
                else -> 1
            }])
            nameElement.prefMaxSize = Vec2i(max(0, maxSize.x - pingElement.size.x), HEIGHT)
            lastPing = ping
        }
        val displayName = BaseComponent()
        item.team?.prefix?.let {
            displayName += it
        }
        displayName += item.displayName.apply {
            // ToDo: Set correct formatting code
            val color = item.team?.formattingCode
            if (color is RGBColor) {
                applyDefaultColor(color)
            }
        }
        item.team?.suffix?.let {
            displayName += it
        }

        if (forcePrepare || displayName !== lastDisplayName) {
            nameElement.text = displayName
            lastDisplayName = displayName
        }

        this.prefSize = Vec2i((PADDING * 2) + nameElement.prefSize.x + INNER_MARGIN + pingElement.prefSize.x, HEIGHT)
        background.size = size
        forcePrepare = false
    }

    override fun onParentChange() {
        forcePrepare = true
    }


    companion object {
        const val HEIGHT = 10
        const val INNER_MARGIN = 5
        const val PADDING = 1
    }
}
