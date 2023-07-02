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

package de.bixilon.minosoft.gui.rendering.gui.elements.input.checkbox

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.GuiDelegate
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.SingleChildrenManager
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.VerticalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonStyle
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.util.KUtil.toResourceLocation

open class SwitchElement(
    guiRenderer: GUIRenderer,
    text: Any,
    state: Boolean = false,
    disabled: Boolean = false,
    parent: Element?,
    var onChange: (state: Boolean) -> Unit,
) : AbstractCheckboxElement(guiRenderer), ChildedElement {
    override val children = SingleChildrenManager()
    protected val textElement = TextElement(guiRenderer, text, background = null).apply { this.parent = this@SwitchElement }
    private val style = ButtonStyle(
        guiRenderer.atlasManager["minosoft:switch_disabled"],
        guiRenderer.atlasManager["minosoft:switch_normal"],
        guiRenderer.atlasManager["minosoft:switch_hovered"],
    )

    private val onStateAtlas = guiRenderer.atlasManager["minosoft:switch_state_on"]
    private val offStateAtlas = guiRenderer.atlasManager["minosoft:switch_state_off"]

    var state by GuiDelegate(state)
    var disabled by GuiDelegate(disabled)

    private var hovered: Boolean = false

    override val canFocus: Boolean
        get() = !disabled


    init {
        size = SIZE + Vec2(5 + TEXT_MARGIN + textElement.size.x, 0)
        this.parent = parent
    }

    private fun getTexture() = when {
        disabled -> style.disabled
        hovered -> style.hovered
        else -> style.normal
    } ?: guiRenderer.context.textures.whiteTexture

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val size = size
        val background = AtlasImageElement(guiRenderer, getTexture())
        background.size = SIZE

        background.render(offset, consumer, options)


        if (state) {
            AtlasImageElement(guiRenderer, onStateAtlas, size = SLIDER_SIZE).render(offset + Vec2i(SIZE.x - SLIDER_SIZE.x, 0), consumer, options)
        } else {
            AtlasImageElement(guiRenderer, offStateAtlas, size = SLIDER_SIZE).render(offset, consumer, options)
        }

        textElement.render(offset + Vec2i(SIZE.x + TEXT_MARGIN, VerticalAlignments.CENTER.getOffset(size.y, textElement.size.y)), consumer, options)
    }


    override fun onMouseAction(position: Vec2, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (disabled) {
            return true
        }
        if (button != MouseButtons.LEFT) {
            return true
        }
        if (action != MouseActions.PRESS) {
            return true
        }

        switchState()
        return true
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (!hovered) {
            return true
        }
        if (disabled) {
            return true
        }
        if (key != KeyCodes.KEY_ENTER) {
            return true
        }
        if (type != KeyChangeTypes.PRESS) {
            return true
        }
        switchState()
        return true
    }

    override fun onMouseEnter(position: Vec2, absolute: Vec2): Boolean {
        hovered = true
        context.window.cursorShape = CursorShapes.HAND

        return true
    }

    override fun onMouseLeave(): Boolean {
        hovered = false
        context.window.resetCursor()

        return true
    }

    open fun switchState() {
        state = !state
        if (guiRenderer.connection.profiles.audio.gui.button) {
            guiRenderer.connection.world.play2DSound(CLICK_SOUND)
        }
    }

    private companion object {
        val CLICK_SOUND = "minecraft:ui.button.click".toResourceLocation()
        const val TEXT_MARGIN = 5
        val SIZE = Vec2(30, 20)
        val SLIDER_SIZE = Vec2(6, 20)
    }
}
