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

package de.bixilon.minosoft.gui.rendering.gui.elements.input.button

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.GuiDelegate
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.SingleChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.properties.ButtonProperties
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.Labeled
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.offset
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.spaceSize

open class ButtonElement(
    guiRenderer: GUIRenderer,
    text: Any,
    properties: ButtonProperties = ButtonProperties.DEFAULT,
    style: ButtonStyle? = null,
    var onSubmit: () -> Unit,
) : Element(guiRenderer), ChildedElement, Labeled {
    override val children = SingleChildrenManager()
    protected val textElement = TextElement(guiRenderer, text, background = null, parent = this)
    val style = style ?: ButtonStyle(
        guiRenderer.atlasManager["button_disabled"],
        guiRenderer.atlasManager["button_normal"],
        guiRenderer.atlasManager["button_hovered"]
    )

    override val canFocus: Boolean get() = !properties.disabled

    var properties by GuiDelegate(properties)

    protected var hovered: Boolean = false

    init {
        padding = Vec4(4.0f)
        updateSize()
    }

    protected fun updateSize() {
        if (!properties.dynamic) return

        val text = this.textElement.size
        val size = text + padding.spaceSize
        this.size = size
        invalidate()
    }

    private fun getTexture() = when {
        this::properties.rendering().disabled -> style.disabled
        hovered -> style.hovered
        else -> style.normal
    } ?: guiRenderer.context.textures.whiteTexture


    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val size = size
        AtlasImageElement(guiRenderer, getTexture(), size).render(offset, consumer, options)
        offset += padding.offset

        val textSize = textElement.size
        textElement.render(offset + Vec2(HorizontalAlignments.CENTER.getOffset(size.x, textSize.x), VerticalAlignments.CENTER.getOffset(size.y, textSize.y)), consumer, options)
    }

    override fun onMouseAction(position: Vec2, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (properties.disabled) return true
        if (button != MouseButtons.LEFT) return true
        if (action != MouseActions.PRESS) return true

        submit()
        return true
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (!hovered) return true
        if (properties.disabled) return true
        if (key != KeyCodes.KEY_ENTER) return true
        if (type != KeyChangeTypes.PRESS) return true
        submit()
        return true
    }

    protected fun updateCursor() {
        if (properties.hand && hovered) {
            context.window.cursorShape = CursorShapes.HAND
        } else {
            context.window.resetCursor()
        }
    }

    override fun onMouseEnter(position: Vec2, absolute: Vec2): Boolean {
        hovered = true
        updateCursor()
        return true
    }

    override fun onMouseLeave(): Boolean {
        hovered = false
        updateCursor()
        return true
    }


    protected open fun submit() {
        val sound = properties.sound
        if (sound.click != null && guiRenderer.connection.profiles.audio.gui.button) {
            guiRenderer.connection.world.play2DSound(sound.click)
        }
        onSubmit.invoke()
    }

    override var text by textElement::text
    override var chatComponent by textElement::chatComponent
    override var textProperties by textElement::textProperties
}
