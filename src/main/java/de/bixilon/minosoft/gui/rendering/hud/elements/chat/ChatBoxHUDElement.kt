/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements.chat

import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.input.SubmittableTextField
import glm_.vec2.Vec2i

class ChatBoxHUDElement(hudRenderer: HUDRenderer) : HUDElement(hudRenderer) {
    private lateinit var inputField: SubmittableTextField

    override fun init() {
        inputField = SubmittableTextField(font = hudRenderer.renderWindow.font, z = 100, maxLength = 256, onSubmit = {
            try {
                hudRenderer.renderWindow.connection.sender.sendChatMessage(it)
                closeChat()
                return@SubmittableTextField true
            } catch (exception: Exception) {
                closeChat()
                return@SubmittableTextField false
            }
        })

        layout.addChild(inputField)

        hudRenderer.renderWindow.registerKeyCallback(KeyBindingsNames.OPEN_CHAT) { _, _ ->
            openChat()
        }
        hudRenderer.renderWindow.registerKeyCallback(KeyBindingsNames.CLOSE_CHAT) { _, _ ->
            closeChat()
        }
    }

    override fun screenChangeResizeCallback(screenDimensions: Vec2i) {
        layout.fakeX = screenDimensions.x
    }

    fun openChat() {
        hudRenderer.renderWindow.currentKeyConsumer = inputField
        hudRenderer.renderWindow.currentElement.remove(KeyBindingsNames.WHEN_IN_GAME)
        hudRenderer.renderWindow.currentElement.add(KeyBindingsNames.WHEN_IN_CHAT)
    }

    fun closeChat() {
        inputField.clearText()
        hudRenderer.renderWindow.currentKeyConsumer = null
        hudRenderer.renderWindow.currentElement.remove(KeyBindingsNames.WHEN_IN_CHAT)
        hudRenderer.renderWindow.currentElement.add(KeyBindingsNames.WHEN_IN_GAME)
    }
}
