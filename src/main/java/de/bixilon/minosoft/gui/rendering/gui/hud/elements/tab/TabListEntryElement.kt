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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.tab

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.DynamicImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.util.KUtil.nullCompare
import java.util.*

class TabListEntryElement(
    guiRenderer: GUIRenderer,
    val tabList: TabListElement,
    uuid: UUID,
    val item: PlayerAdditional,
    width: Float,
) : Element(guiRenderer), Pollable, Comparable<TabListEntryElement> {

    init {
        _parent = tabList
    }

    private val background: ColorElement

    private val skinElement = DynamicImageElement(guiRenderer, null, uvStart = Vec2(0.125), uvEnd = Vec2(0.25), size = Vec2(8, 8), parent = this)

    // private val skinElement = ImageElement(guiRenderer, guiRenderer.context.textureManager.steveTexture, uvStart = Vec2(0.125), uvEnd = Vec2(0.25), size = Vec2i(512, 512))
    private val nameElement = TextElement(guiRenderer, "", background = null, parent = this)
    private lateinit var pingElement: AtlasImageElement

    private var displayName: ChatComponent = item.tabDisplayName
    private var ping = item.ping
    private var gamemode: Gamemodes = item.gamemode
    private var name: String = item.name
    private var teamName = item.team?.name

    override var prefSize: Vec2 = Vec2.EMPTY
    override var prefMaxSize: Vec2
        get() = Vec2(width, HEIGHT)
        set(value) = Unit
    override var size: Vec2
        get() = maxSize
        set(value) = Unit

    var width: Float = width
        set(value) {
            if (value == field) {
                return
            }
            field = value
            forceApply()
        }

    init {
        background = ColorElement(guiRenderer, size, RGBColor(120, 120, 120, 130))
        DefaultThreadPool += { skinElement.texture = context.textureManager.skins.getSkin(uuid, item.properties, fetch = guiRenderer.connection.network.encrypted)?.texture }
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        background.render(offset, consumer, options)
        skinElement.render(offset + Vec2(PADDING, PADDING), consumer, options)
        nameElement.render(offset + Vec2(skinElement.size.x + PADDING * 3, PADDING), consumer, options)
        pingElement.render(offset + Vec2(HorizontalAlignments.RIGHT.getOffset(maxSize.x, pingElement.size.x + PADDING), PADDING), consumer, options)
    }

    override fun forceSilentApply() {
        // ToDo (Performance): If something changed, should we just prepare the changed
        pingElement = AtlasImageElement(
            guiRenderer, tabList.pingBarsAtlasElements[when {
                ping < 0 -> 0
                ping < 150 -> 5
                ping < 300 -> 4
                ping < 600 -> 3
                ping < 1000 -> 2
                else -> 1
            }]
        )
        nameElement.prefMaxSize = Vec2(maxOf(0.0f, maxSize.x - pingElement.size.x - skinElement.size.x - INNER_MARGIN), HEIGHT)

        nameElement.text = displayName

        this.prefSize = Vec2((PADDING * 6) + skinElement.size.x + nameElement.prefSize.x + INNER_MARGIN + pingElement.prefSize.x, HEIGHT)
        background.size = size
        cacheUpToDate = false
    }

    override fun poll(): Boolean {
        val displayName = item.tabDisplayName
        val ping = item.ping
        val gamemode = item.gamemode
        val name = item.name
        val teamName = item.team?.name

        if (this.ping == ping && this.displayName == displayName && this.gamemode == gamemode && this.name == name && this.teamName == teamName) {
            return false
        }

        this.ping = ping
        this.displayName = displayName
        this.gamemode = gamemode
        this.name = name
        this.teamName = teamName

        return true
    }

    override fun compareTo(other: TabListEntryElement): Int {
        if (this.gamemode != other.gamemode) {
            if (this.gamemode == Gamemodes.SPECTATOR) {
                return -1
            }
            if (other.gamemode == Gamemodes.SPECTATOR) {
                return 1
            }
        }

        this.teamName?.nullCompare(other.teamName)?.let { return it }

        this.name.lowercase().nullCompare(other.name.lowercase())?.let { return it }

        return 0
    }

    override fun toString(): String {
        return displayName.legacyText
    }

    companion object {
        const val HEIGHT = 10
        const val INNER_MARGIN = 5
        const val PADDING = 1
    }
}
