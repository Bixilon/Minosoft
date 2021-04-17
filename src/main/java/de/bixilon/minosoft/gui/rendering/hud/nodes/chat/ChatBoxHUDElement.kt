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

package de.bixilon.minosoft.gui.rendering.hud.nodes.chat


import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.elements.input.SubmittableTextField
import de.bixilon.minosoft.gui.rendering.hud.nodes.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.ImageNode
import de.bixilon.minosoft.gui.rendering.hud.nodes.properties.NodeSizing
import de.bixilon.minosoft.gui.rendering.hud.nodes.properties.Spacing
import de.bixilon.minosoft.gui.rendering.util.abstractions.ScreenResizeCallback
import de.bixilon.minosoft.util.MMath
import glm_.vec2.Vec2i

class ChatBoxHUDElement(hudRenderer: HUDRenderer) : HUDElement(hudRenderer), ScreenResizeCallback {
    private lateinit var inputField: SubmittableTextField
    private var inputFieldBackground = ImageNode(hudRenderer.renderWindow, sizing = NodeSizing(margin = Spacing(left = 1, right = 1)), textureLike = hudRenderer.renderWindow.WHITE_TEXTURE, z = 0, tintColor = RenderConstants.TEXT_BACKGROUND_COLOR)

    override fun init() {
        inputField = SubmittableTextField(renderWindow = hudRenderer.renderWindow, maxLength = 256, onSubmit = {
            hudRenderer.renderWindow.connection.sender.sendChatMessage(it)
            closeChat()
        })

        layout.addChild(Vec2i(0, 0), inputField)
        inputField.apply()

        hudRenderer.renderWindow.registerKeyCallback(KeyBindingsNames.OPEN_CHAT) { _, _ ->
            openChat()
        }
        hudRenderer.renderWindow.registerKeyCallback(KeyBindingsNames.CLOSE_CHAT) { _, _ ->
            closeChat()
        }
    }

    override fun onScreenResize(screenDimensions: Vec2i) {
        layout.sizing.minSize.x = screenDimensions.x
        inputFieldBackground.sizing.forceSize = Vec2i(screenDimensions.x - 2, MMath.clamp(inputField.sizing.currentSize.y, Font.CHAR_HEIGHT, Int.MAX_VALUE)) // 2 pixels for margin
        layout.sizing.maxSize.x = screenDimensions.x
        layout.sizing.validate()
        layout.apply()
    }

    fun openChat() {
        layout.addChild(Vec2i(0, 0), inputFieldBackground)
        hudRenderer.renderWindow.currentKeyConsumer = inputField
        hudRenderer.renderWindow.currentElement.remove(KeyBindingsNames.WHEN_IN_GAME)
        hudRenderer.renderWindow.currentElement.add(KeyBindingsNames.WHEN_IN_CHAT)
    }

    fun closeChat() {
        layout.removeChild(inputFieldBackground)
        inputField.clearText()
        hudRenderer.renderWindow.currentKeyConsumer = null
        hudRenderer.renderWindow.currentElement.remove(KeyBindingsNames.WHEN_IN_CHAT)
        hudRenderer.renderWindow.currentElement.add(KeyBindingsNames.WHEN_IN_GAME)
    }
}
