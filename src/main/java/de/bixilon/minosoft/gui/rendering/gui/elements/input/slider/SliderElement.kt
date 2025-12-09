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

package de.bixilon.minosoft.gui.rendering.gui.elements.input.slider

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer
import kotlin.math.roundToInt

class SliderElement(
    guiRenderer: GUIRenderer,
    private val label: String,
    private val min: Float,
    private val max: Float,
    initialValue: Float,
    private val onChange: (Float) -> Unit
) : Element(guiRenderer) {
    private val buttonAtlas = guiRenderer.atlas[BUTTON_ATLAS]
    private val sliderAtlas = guiRenderer.atlas[SLIDER_ATLAS]

    var textElement: TextElement
    private var isDragging = false

    var value: Float = initialValue.coerceIn(min, max)
        set(value) {
            val clamped = value.coerceIn(min, max)
            if (field != clamped) {
                field = clamped
                updateText()
                onChange(clamped)
                cacheUpToDate = false
            }
        }

    init {
        textElement = TextElement(guiRenderer, getDisplayText(), background = null, parent = this)
        updateText()
        // Set initial size based on text element size with padding (similar to ButtonElement)
        size = Vec2f(textElement.size.x + TEXT_PADDING * 2, 20.0f)
    }

    private fun getDisplayText(): String {
        return "$label: ${value.roundToInt()}"
    }

    private fun updateText() {
        textElement.text = getDisplayText()
        textElement.silentApply()
        if (size.x == 0.0f) {
            size = Vec2f(textElement.size.x + TEXT_PADDING * 2, 20.0f)
        }
    }

    override fun forceSilentApply() {
        textElement.silentApply()
        cacheUpToDate = false
    }

    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
        val size = size
        val sliderHeight = 20.0f

        val background = AtlasImageElement(guiRenderer, buttonAtlas?.get("normal") ?: guiRenderer.context.textures.whiteTexture)
        background.size = size
        background.render(offset, consumer, options)

        val normalizedValue = (value - min) / (max - min)
        val sliderWidth = 8.0f
        val trackWidth = size.x - sliderWidth
        val sliderX = trackWidth * normalizedValue
        val slider = AtlasImageElement(guiRenderer, buttonAtlas?.get("hovered") ?: guiRenderer.context.textures.whiteTexture)
        slider.size = Vec2f(sliderWidth, sliderHeight)
        slider.render(offset + Vec2f(sliderX, 0.0f), consumer, options)
        val textSize = textElement.size
        val textX = (size.x - textSize.x) / 2
        val textY = (size.y - textSize.y) / 2
        textElement.render(offset + Vec2f(textX, textY), consumer, options)
    }

    override fun onMouseAction(position: Vec2f, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (button != MouseButtons.LEFT) {
            return true
        }

        when (action) {
            MouseActions.PRESS -> {
                isDragging = true
                updateValueFromPosition(position.x)
            }
            MouseActions.RELEASE -> {
                isDragging = false
            }
        }

        return true
    }

    override fun onMouseMove(position: Vec2f, absolute: Vec2f): Boolean {
        if (isDragging) {
            updateValueFromPosition(position.x)
        }
        return true
    }

    override fun onMouseLeave(): Boolean {
        isDragging = false
        return super.onMouseLeave()
    }

    private fun updateValueFromPosition(x: Float) {
        val sliderWidth = 8.0f
        val trackWidth = size.x - sliderWidth

        if (trackWidth <= 0) {
            value = min
            return
        }

        val normalizedX = (x - sliderWidth / 2) / trackWidth
        value = min + normalizedX * (max - min)
    }

    companion object {
        val BUTTON_ATLAS = minecraft("elements/button")
        val SLIDER_ATLAS = minecraft("elements/slider")
        private const val TEXT_PADDING = 4.0f
    }
}

