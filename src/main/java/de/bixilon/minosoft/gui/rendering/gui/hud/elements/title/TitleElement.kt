/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.collection.SetChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.ScreenPositionedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.fade.FadingTextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.fade.FadingTimes
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.modding.event.events.title.*
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.Initializable
import de.bixilon.minosoft.util.KUtil.toResourceLocation

// ToDo: Remove subtitle when hidden
class TitleElement(guiRenderer: GUIRenderer) : Element(guiRenderer), ScreenPositionedElement, Initializable, ChildedElement {
    override val children = SetChildrenManager(this)
    val title = FadingTextElement(guiRenderer, "", background = null, properties = TextRenderProperties(scale = 4.0f), parent = this)
    val subtitle = FadingTextElement(guiRenderer, "", background = null, properties = TextRenderProperties(scale = 2.0f), parent = this)
    var times: FadingTimes = FadingTimes.EMPTY
        set(value) {
            if (field == value) return
            field = value
            title.times = value
            subtitle.times = value
        }

    override val screenOffset: Vec2
        get() {
            val layoutOffset = Vec2.EMPTY

            val scaledSize = guiRenderer.screen.scaled

            layoutOffset.x = (scaledSize.x - super.size.x / 2) / 2
            layoutOffset.y = (scaledSize.y / 2 - title.size.y)

            return layoutOffset
        }

    init {
        times = DEFAULT_TIMES
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val size = size
        title.render(offset + Vec2(HorizontalAlignments.CENTER.getOffset(size.x, title.size.x), 0), consumer, options)
        subtitle.render(offset + Vec2(HorizontalAlignments.CENTER.getOffset(size.x, subtitle.size.x), title.size.y + SUBTITLE_VERTICAL_OFFSET), consumer, options)
    }

    override fun update() {
        val size = title.size

        size.x = maxOf(size.x, subtitle.size.x)
        size.y += subtitle.size.y

        this.size = size
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
        title.hide(true)
        subtitle.hide(true)
        title.text = ""
        subtitle.text = ""

        times = DEFAULT_TIMES
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
            this.show()
        }
        connection.events.listen<TitleTimesSetEvent> {
            this.times = FadingTimes(it.fadeInTime * ProtocolDefinition.TICK_TIME, it.stayTime * ProtocolDefinition.TICK_TIME, it.fadeOutTime * ProtocolDefinition.TICK_TIME)
        }
    }

    companion object : HUDBuilder<LayoutedGUIElement<TitleElement>> {
        override val identifier: ResourceLocation = "minosoft:title".toResourceLocation()
        const val SUBTITLE_VERTICAL_OFFSET = 10
        private val DEFAULT_TIMES = FadingTimes(
            20 * ProtocolDefinition.TICK_TIME,
            60 * ProtocolDefinition.TICK_TIME,
            20 * ProtocolDefinition.TICK_TIME,
        )

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<TitleElement> {
            return LayoutedGUIElement(TitleElement(guiRenderer))
        }
    }
}
