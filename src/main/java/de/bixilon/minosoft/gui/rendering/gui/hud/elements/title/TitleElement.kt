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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.title

import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.text.FadingTextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec2.Vec2i
import java.lang.Integer.max

class TitleElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    val title = FadingTextElement(hudRenderer, "", scale = 4.0f, parent = this)
    val subtitle = FadingTextElement(hudRenderer, "", parent = this)
    var fadeInTime = 0L
        set(value) {
            title.fadeInTime = value
            subtitle.fadeInTime = value
            field = value
        }
    var stayTime = 0L
        set(value) {
            title.stayTime = value
            subtitle.stayTime = value
            field = value
        }
    var fadeOutTime = 0L
        set(value) {
            title.fadeOutTime = value
            subtitle.fadeOutTime = value
            field = value
        }

    init {
        fadeInTime = DEFAULT_FADE_IN_TIME
        stayTime = DEFAULT_STAY_TIME
        fadeOutTime = DEFAULT_FADE_OUT_TIME
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        val size = size
        title.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, title.size.x), 0), z, consumer, options)
        subtitle.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, subtitle.size.x), title.size.y + SUBTITLE_VERTICAL_OFFSET), z, consumer, options)


        return TextElement.LAYERS
    }

    override fun forceSilentApply() {
        val size = title.size

        size.x = max(size.x, subtitle.size.x)
        size.y += subtitle.size.y

        this._size = size
    }

    fun show() {
        title.show()
        subtitle.show()
    }

    fun hide() {
        title.hide()
        subtitle.hide()
    }

    fun reset() {
        title.forceHide()
        subtitle.forceHide()
        title.text = ""
        subtitle.text = ""

        fadeInTime = DEFAULT_FADE_IN_TIME
        stayTime = DEFAULT_STAY_TIME
        fadeOutTime = DEFAULT_FADE_OUT_TIME
    }

    companion object {
        const val SUBTITLE_VERTICAL_OFFSET = 10
        const val DEFAULT_FADE_IN_TIME = 20L * ProtocolDefinition.TICK_TIME
        const val DEFAULT_STAY_TIME = 60L * ProtocolDefinition.TICK_TIME
        const val DEFAULT_FADE_OUT_TIME = 20L * ProtocolDefinition.TICK_TIME
    }
}
