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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.util.KUtil.format

abstract class WawlaElement(protected val wawla: WawlaHUDElement) : Element(wawla.guiRenderer) {
    abstract val elements: List<Element?>


    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        for (element in elements) {
            if (element == null) continue

            element.render(offset, consumer, options)
            offset.y += element.size.y
        }
    }

    override fun forceSilentApply() {
        val size = Vec2.EMPTY

        for (element in elements) {
            if (element == null) continue

            val elementSize = element.size
            size.x = maxOf(size.x, elementSize.x)
            size.y += elementSize.y
        }

        this.size = size
    }

    protected fun createNameElement(translationKey: ResourceLocation?, fallback: ChatComponent): TextElement {
        val name = wawla.context.connection.language.translate(translationKey) ?: fallback
        name.setFallbackColor(ChatColors.WHITE)
        return TextElement(guiRenderer, name, background = null, properties = TextRenderProperties(scale = 1.25f))
    }

    protected fun createIdentifierElement(item: Identified): TextElement? {
        if (!wawla.profile.identifier) {
            return null
        }
        return TextElement(guiRenderer, item.identifier.format(), background = null, properties = TextRenderProperties(scale = 1.2f))
    }
}
