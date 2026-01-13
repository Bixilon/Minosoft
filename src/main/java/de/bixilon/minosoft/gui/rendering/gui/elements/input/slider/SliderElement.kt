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
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.input.MouseCapturing
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions.Companion.copy
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import kotlin.math.roundToInt

class SliderElement(
    guiRenderer: GUIRenderer,
    private val label: String,
    private val min: Float,
    private val max: Float,
    initialValue: Float,
    private val onChange: (Float) -> Unit
) : Element(guiRenderer), MouseCapturing {
    private val buttonAtlas = guiRenderer.atlas[BUTTON_ATLAS]

    var textElement: TextElement
    private var isDragging = false
    private var isHovered = false
    private var isHandleHovered = false
    
    var disabled: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            cacheUpToDate = false
        }
    
    override val isCapturingMouse: Boolean get() = isDragging

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
        size = Vec2f(textElement.size.x + TEXT_PADDING * 2, SLIDER_HEIGHT)
    }

    private fun getDisplayText(): String {
        return "$label: ${value.roundToInt()}"
    }

    private fun updateText() {
        textElement.text = getDisplayText()
        textElement.silentApply()
        if (size.x == 0.0f) {
            size = Vec2f(textElement.size.x + TEXT_PADDING * 2, SLIDER_HEIGHT)
        }
    }

    override fun forceSilentApply() {
        textElement.silentApply()
        cacheUpToDate = false
    }

    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
        val size = size
        val renderOptions = if (disabled) options.copy(alpha = 0.4f) else options

        val trackTexture = buttonAtlas?.get("disabled") ?: guiRenderer.context.textures.whiteTexture
        val trackBackground = AtlasImageElement(guiRenderer, trackTexture)
        trackBackground.size = size
        trackBackground.render(offset, consumer, renderOptions)

        val normalizedValue = (value - min) / (max - min)
        val handleWidth = HANDLE_WIDTH
        val trackWidth = size.x - handleWidth
        val handleX = trackWidth * normalizedValue
        
        val handleTexture = if (disabled) {
            buttonAtlas?.get("disabled")
        } else if (isHandleHovered || isDragging) {
            buttonAtlas?.get("hovered")
        } else {
            buttonAtlas?.get("normal")
        } ?: guiRenderer.context.textures.whiteTexture
        
        val slider = AtlasImageElement(guiRenderer, handleTexture)
        slider.size = Vec2f(handleWidth, SLIDER_HEIGHT)
        slider.render(offset + Vec2f(handleX, 0.0f), consumer, renderOptions)
        
        val textSize = textElement.size
        val textX = (size.x - textSize.x) / 2
        val textY = (size.y - textSize.y) / 2
        textElement.render(offset + Vec2f(textX, textY), consumer, renderOptions)
    }

    override fun onMouseAction(position: Vec2f, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (disabled) {
            return true
        }
        if (button != MouseButtons.LEFT) {
            return true
        }

        when (action) {
            MouseActions.PRESS -> {
                isDragging = true
                updateValueFromPosition(position.x)
                cacheUpToDate = false
            }
            MouseActions.RELEASE -> {
                isDragging = false
                context.window.resetCursor()
                cacheUpToDate = false
            }
        }

        return true
    }

    override fun onMouseMove(position: Vec2f, absolute: Vec2f): Boolean {
        if (isDragging) {
            updateValueFromPosition(position.x)
        }
        
        if (!isDragging) {
            val normalizedValue = (value - min) / (max - min)
            val handleWidth = HANDLE_WIDTH
            val trackWidth = size.x - handleWidth
            val handleX = trackWidth * normalizedValue
            
            val wasHandleHovered = isHandleHovered
            isHandleHovered = position.x >= handleX && position.x < handleX + handleWidth
            
            if (wasHandleHovered != isHandleHovered) {
                cacheUpToDate = false
            }
        }
        
        return true
    }

    override fun onMouseEnter(position: Vec2f, absolute: Vec2f): Boolean {
        isHovered = true
        context.window.cursorShape = CursorShapes.HAND
        
        val normalizedValue = (value - min) / (max - min)
        val handleWidth = HANDLE_WIDTH
        val trackWidth = size.x - handleWidth
        val handleX = trackWidth * normalizedValue
        
        isHandleHovered = position.x >= handleX && position.x < handleX + handleWidth
        cacheUpToDate = false
        return true
    }

    override fun onMouseLeave(): Boolean {
        isHovered = false
        isHandleHovered = false
        if (!isDragging) {
            context.window.resetCursor()
        }
        cacheUpToDate = false
        return super.onMouseLeave()
    }
    
    override fun onMouseActionOutside(relativeX: Float, button: MouseButtons, action: MouseActions): Boolean {
        if (!isDragging) return false
        
        if (button == MouseButtons.LEFT && action == MouseActions.RELEASE) {
            isDragging = false
            context.window.resetCursor()
            cacheUpToDate = false
            return true
        }
        return false
    }
    
    override fun onMouseMoveOutside(relativeX: Float): Boolean {
        if (!isDragging) return false
        updateValueFromPosition(relativeX)
        return true
    }

    private fun updateValueFromPosition(x: Float) {
        val handleWidth = HANDLE_WIDTH
        val trackWidth = size.x - handleWidth

        if (trackWidth <= 0) {
            value = min
            return
        }

        val normalizedX = (x - handleWidth / 2) / trackWidth
        value = min + normalizedX * (max - min)
    }

    companion object {
        val BUTTON_ATLAS = minecraft("elements/button")
        private const val TEXT_PADDING = 4.0f
        private const val SLIDER_HEIGHT = 20.0f
        private const val HANDLE_WIDTH = 8.0f
    }
}

