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

package de.bixilon.minosoft.gui.rendering.gui.elements.text

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.text.background.TextBackground
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions.Companion.copy
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY

class FadingTextElement(
    guiRenderer: GUIRenderer,
    text: Any = "",
    var fadeInTime: Long = 100,
    var stayTime: Long = 1000,
    var fadeOutTime: Long = 100,
    background: TextBackground? = TextBackground.DEFAULT,
    parent: Element? = null,
    properties: TextRenderProperties,
) : TextElement(guiRenderer = guiRenderer, text = text, background = background, parent, properties), Pollable {
    override var cacheEnabled: Boolean
        get() {
            if (hidden || !super.cacheEnabled) {
                return false
            }
            val time = millis()
            return (time >= fadeInEndTime) && (time < fadeOutStartTime)
        }
        set(value) {
            super.cacheEnabled = value
        }
    override var size: Vec2i
        get() {
            return hidden.decide({ Vec2i.EMPTY }, { super.size })
        }
        set(value) {
            super.size = value
        }
    var hidden: Boolean = false
        private set(value) {
            if (field == value) {
                return
            }
            field = value
            parent?.onChildChange(this) // size changed
        }

    private var fadeInStartTime = -1L
    private var fadeInEndTime = -1L
    private var fadeOutStartTime = -1L
    private var fadeOutEndTime = -1L

    private var alpha = 1.0f

    init {
        show()
    }

    fun show() {
        val time = millis()
        if (time in (fadeInEndTime + 1) until fadeOutStartTime) {
            fadeOutStartTime = time + stayTime
        } else {
            fadeInStartTime = time
            fadeInEndTime = fadeInStartTime + fadeInTime
            fadeOutStartTime = fadeInEndTime + stayTime
        }
        fadeOutEndTime = fadeOutStartTime + fadeOutTime
        hidden = false
    }

    fun hide() {
        if (hidden) {
            return
        }
        // ToDo: Eventually fade out when fading in
        val time = millis()
        fadeInStartTime = -1L
        fadeInEndTime = -1L
        fadeOutStartTime = time
        fadeOutEndTime = fadeOutStartTime + fadeOutTime
    }

    fun forceHide() {
        hidden = true
    }

    override fun poll(): Boolean {
        if (hidden) {
            return false
        }
        val hidden = millis() > fadeOutEndTime
        if (this.hidden != hidden) {
            this.hidden = hidden
            return true
        }
        return false
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (hidden) {
            return
        }
        val time = millis()
        if (time > fadeOutEndTime) {
            return
        }

        alpha = 1.0f

        if (time < fadeInEndTime) {
            alpha = 1.0f - (fadeInEndTime - time) / fadeInTime.toFloat()
        } else if (time < fadeOutStartTime) {
            alpha = 1.0f
            return super.forceRender(offset, consumer, options) // ToDo: Cache
        } else if (time < fadeOutEndTime) {
            alpha = (fadeOutEndTime - time) / fadeOutTime.toFloat()
        } else {
            return
        }

        val nextOptions = options?.copy(alpha = options.alpha * alpha) ?: (alpha == 1.0f).decide(null) { GUIVertexOptions(alpha = alpha) }

        super.forceRender(offset, consumer, nextOptions)
    }
}
