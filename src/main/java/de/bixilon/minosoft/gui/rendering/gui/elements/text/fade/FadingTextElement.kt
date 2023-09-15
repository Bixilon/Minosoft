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

package de.bixilon.minosoft.gui.rendering.gui.elements.text.fade

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.background.TextBackground
import de.bixilon.minosoft.gui.rendering.gui.elements.text.fade.FadePhase.Companion.createPhase
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions.Companion.copy
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

class FadingTextElement(
    guiRenderer: GUIRenderer,
    text: Any = "",
    times: FadingTimes = FadingTimes.DEFAULT,
    background: TextBackground? = TextBackground.DEFAULT,
    parent: Element? = null,
    properties: TextRenderProperties,
) : TextElement(guiRenderer = guiRenderer, text = text, background = background, parent, properties) {
    private var phase: FadePhase? = null

    override var cacheEnabled: Boolean
        get() {
            if (!super.cacheEnabled) return false
            if (this.phase == null) return true
            updatePhase(millis())
            return phase != null
        }
        set(value) {
            super.cacheEnabled = value
        }

    var times: FadingTimes = times
        set(value) {
            if (field == value) return
            field = value
            show()
        }


    init {
        show()
    }

    private fun updateSize(phase: FadePhase?) {
        this._size = if (phase == null) Vec2.EMPTY else info.size.withBackgroundSize()
    }

    fun show() {
        val time = millis()
        val phase = times.createPhase(time)
        this.phase = phase
        updateSize(phase)

        parent?.onChildChange(this)
        cacheEnabled = false
    }

    fun hide(force: Boolean = false) {
        this.phase = if (force) null else this.phase?.stop(millis())
        parent?.onChildChange(this)
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (phase == null) return
        val millis = millis()
        this.updatePhase(millis)
        val phase = this.phase ?: return
        val alpha = phase.getAlpha(millis)

        super.forceRender(offset, consumer, options.copy(alpha = alpha))
    }

    private fun updatePhase(millis: Long) {
        val phase = this.phase ?: return
        if (!phase.isDone(millis)) return
        val next = phase.next(millis)
        this.phase = next
        updateSize(next)
        if (next == null) {
            // animation end
            cacheEnabled = true
        } else {
            cacheEnabled = false
        }
    }
}
