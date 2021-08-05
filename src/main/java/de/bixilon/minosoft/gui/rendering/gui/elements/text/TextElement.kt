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

package de.bixilon.minosoft.gui.rendering.gui.elements.text

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.renderer.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import glm_.vec2.Vec2i

class TextElement(
    private val renderWindow: RenderWindow,
    text: Any,
) : LabeledElement() {
    override var text: Any = text
        set(value) {
            textComponent = ChatComponent.of(value)
            field = value
            prepared = false
        }

    override var textComponent: ChatComponent = ChatComponent.of("")
        private set(value) {
            size = minSize
            if (value.message.isNotEmpty()) {
                val size = Vec2i(0, 0)
                ChatComponentRenderer.render(Vec2i(0, 0), Vec2i(0, 0), size, 0, this, renderWindow, null, value)
                this.size = size
            }
            field = value
        }

    init {
        textComponent = ChatComponent.of(text)
    }


    override fun render(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        ChatComponentRenderer.render(Vec2i(offset), offset, Vec2i(0, 0), z, this, renderWindow, consumer, textComponent)
        prepared = true
        return LAYERS
    }

    companion object {
        const val LAYERS = 4 // 1 layer for the text, 1 for strikethrough. * 2 for shadow
    }
}
