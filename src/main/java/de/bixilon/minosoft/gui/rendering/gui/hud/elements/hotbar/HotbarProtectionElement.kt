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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.GuiDelegate
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

class HotbarProtectionElement(guiRenderer: GUIRenderer) : Element(guiRenderer) {
    private val emptyProtection = guiRenderer.atlasManager["minecraft:empty_protection"]
    private val halfProtection = guiRenderer.atlasManager["minecraft:half_protection"]
    private val fullProtection = guiRenderer.atlasManager["minecraft:full_protection"]

    private var protection by GuiDelegate(0.0f)

    init {
        tryUpdate()
        //TODO: context.connection.player.equipment::equipment.observeMap(this) { protection = guiRenderer.context.connection.player.equipment.getProtection() }
    }


    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val protection = this::protection.rendering()
        if (protection <= 0.0f) {
            return
        }

        var protectionLeft = protection

        for (i in 0 until 10) {
            val atlasElement = when {
                protectionLeft < 1.0f -> emptyProtection
                protectionLeft < 2.0f -> halfProtection
                else -> fullProtection
            }

            val image = AtlasImageElement(guiRenderer, atlasElement)

            image.render(offset + Vec2i(i * ARMOR_SIZE.x, 0), consumer, options)

            protectionLeft -= 2.0f
        }
    }

    override fun update() {
        val protection = this::protection.acknowledge()

        size = if (protection <= 0.0f) Vec2.EMPTY else SIZE
    }


    companion object {
        private val ARMOR_SIZE = Vec2i(8, 9)
        private val SIZE = Vec2(10 * ARMOR_SIZE.x, ARMOR_SIZE.y)
    }
}
