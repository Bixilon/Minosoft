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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.block

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.registries.blocks.wawla.BlockWawlaProvider
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.WawlaElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.wawla.WawlaHUDElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

class BlockWawlaElement(wawla: WawlaHUDElement, private val target: BlockTarget) : WawlaElement(wawla) {
    private val name = createName()
    private val additional = createAdditionalInformation()
    private val mod = createMod()

    init {
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        // TODO: render block

        name.render(offset, consumer, options)
        offset.y += name.size.y

        additional?.let { it.render(offset, consumer, options); offset.y += it.size.y }

        mod?.let { it.render(offset, consumer, options); offset.y += it.size.y }
    }

    override fun forceSilentApply() {
        val size = Vec2i(
            x = maxOf(name.size.x, mod?.size?.x ?: 0, additional?.size?.x ?: 0),
            y = name.size.y + (mod?.size?.y ?: 0) + (additional?.size?.y ?: 0),
        )

        this.size = size
    }

    private fun createName(): TextElement {
        return createNameElement(target.blockState.block.item.translationKey) // TODO: use key of block and not item
    }

    private fun createMod(): TextElement? {
        val namespace = target.blockState.block.identifier.namespace
        if (namespace == Namespaces.DEFAULT) {
            return null
        }
        return TextElement(guiRenderer, TextComponent(namespace).color(ChatColors.BLUE), background = false)
    }

    private fun createAdditionalInformation(): TextElement? {
        val component = BaseComponent()

        if (target.blockState.block is BlockWawlaProvider) {
            component += target.blockState.block.getWawlaInformation(context.connection, target)
            component += "\n"
        }
        if (target.entity is BlockWawlaProvider) {
            component += target.entity.getWawlaInformation(context.connection, target)
            component += "\n"
        }

        if (component.parts.isEmpty()) return null

        if (component.parts.last().toString() == "\n") {
            component.parts.removeLast()
        }
        component.setFallbackColor(ChatColors.GRAY)

        return TextElement(guiRenderer, component, background = false)
    }
}
