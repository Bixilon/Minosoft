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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.title

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.FadingTextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.modding.event.events.title.*
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.lang.Integer.max

// ToDo: Remove subtitle when hidden
class TitleElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, Initializable {
    val title = FadingTextElement(guiRenderer, "", background = false, scale = 4.0f, parent = this)
    val subtitle = FadingTextElement(guiRenderer, "", background = false, scale = 2.0f, parent = this)
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
    override var cacheEnabled: Boolean
        get() = super.cacheEnabled && title.cacheEnabled && subtitle.cacheEnabled
        set(value) {
            super.cacheEnabled = value
        }
    override var cacheUpToDate: Boolean
        get() = super.cacheUpToDate && title.cacheUpToDate && subtitle.cacheUpToDate
        set(value) {
            super.cacheEnabled = value
        }

    override val layoutOffset: Vec2i
        get() {
            val layoutOffset = Vec2i.EMPTY

            val scaledSize = guiRenderer.scaledSize

            layoutOffset.x = (scaledSize.x - super.size.x / 2) / 2
            layoutOffset.y = (scaledSize.y / 2 - title.size.y)

            return layoutOffset
        }

    init {
        fadeInTime = DEFAULT_FADE_IN_TIME
        stayTime = DEFAULT_STAY_TIME
        fadeOutTime = DEFAULT_FADE_OUT_TIME
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val time = millis()
        if (time > fadeOutTime) {
            return
        }
        val size = size
        title.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, title.size.x), 0), consumer, options)
        subtitle.render(offset + Vec2i(HorizontalAlignments.CENTER.getOffset(size.x, subtitle.size.x), title.size.y + SUBTITLE_VERTICAL_OFFSET), consumer, options)
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

    override fun init() {
        val connection = context.connection

        connection.events.listen<TitleResetEvent> {
            this.reset()
        }
        connection.events.listen<TitleHideEvent> {
            this.hide()
        }
        connection.events.listen<TitleSetEvent> {
            this.title.text = it.title
            this.show()
        }
        connection.events.listen<TitleSubtitleSetEvent> {
            this.subtitle.text = it.subtitle
            // layout.show() // non vanilla behavior
        }
        connection.events.listen<TitleTimesSetEvent> {
            this.fadeInTime = it.fadeInTime * ProtocolDefinition.TICK_TIME.toLong()
            this.stayTime = it.stayTime * ProtocolDefinition.TICK_TIME.toLong()
            this.fadeOutTime = it.fadeOutTime * ProtocolDefinition.TICK_TIME.toLong()
        }
    }

    companion object : HUDBuilder<LayoutedGUIElement<TitleElement>> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:title".toResourceLocation()
        const val SUBTITLE_VERTICAL_OFFSET = 10
        const val DEFAULT_FADE_IN_TIME = 20L * ProtocolDefinition.TICK_TIME
        const val DEFAULT_STAY_TIME = 60L * ProtocolDefinition.TICK_TIME
        const val DEFAULT_FADE_OUT_TIME = 20L * ProtocolDefinition.TICK_TIME

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<TitleElement> {
            return LayoutedGUIElement(TitleElement(guiRenderer))
        }
    }
}
