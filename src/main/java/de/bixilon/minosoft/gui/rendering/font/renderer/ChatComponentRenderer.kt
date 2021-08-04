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

package de.bixilon.minosoft.gui.rendering.font.renderer

import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.elements.text.LabeledElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import glm_.vec2.Vec2i

interface ChatComponentRenderer<T : ChatComponent> {

    fun render(offset: Vec2i, element: LabeledElement, renderWindow: RenderWindow, consumer: GUIVertexConsumer, text: T)


    companion object {

        fun render(offset: Vec2i, element: LabeledElement, renderWindow: RenderWindow, consumer: GUIVertexConsumer, text: ChatComponent) {
            when (text) {
                is BaseComponent -> BaseComponentRenderer.render(offset, element, renderWindow, consumer, text)
                is TextComponent -> TextComponentRenderer.render(offset, element, renderWindow, consumer, text)
                else -> TODO("Don't know how to render ${text::class.java}")
            }
        }
    }
}
