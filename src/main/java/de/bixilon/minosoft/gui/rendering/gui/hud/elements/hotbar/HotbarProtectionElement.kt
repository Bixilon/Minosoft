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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ImageElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.util.vec.Vec2Util.EMPTY
import glm_.vec2.Vec2i

class HotbarProtectionElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    private val emptyProtection = hudRenderer.atlasManager["minecraft:empty_protection"]!!
    private val halfProtection = hudRenderer.atlasManager["minecraft:half_protection"]!!
    private val fullProtection = hudRenderer.atlasManager["minecraft:full_protection"]!!

    init {
        silentApply()
    }

    private var protection = 0.0f

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer): Int {
        if (protection <= 0.0f) {
            return 0
        }

        var protectionLeft = protection

        for (i in 0 until 10) {
            val atlasElement = when {
                protectionLeft < 1.0f -> emptyProtection
                protectionLeft < 2.0f -> halfProtection
                else -> fullProtection
            }

            val image = ImageElement(hudRenderer, atlasElement)

            image.render(offset + Vec2i(i * ARMOR_SIZE.x, 0), z, consumer)

            protectionLeft -= 2.0f
        }

        return 1
    }

    override fun onParentChange() {
        silentApply()
    }

    override fun silentApply() {
        val protection = hudRenderer.connection.player.protectionLevel // ToDo: Check for equipment change

        if (this.protection == protection) {
            return
        }

        size = if (protection <= 0.0f) {
            Vec2i.EMPTY
        } else {
            SIZE
        }
        this.protection = protection
        cacheUpToDate = false
    }

    companion object {
        private val ARMOR_SIZE = Vec2i(8, 9)
        private val SIZE = Vec2i(10 * ARMOR_SIZE.x, ARMOR_SIZE.y)
    }
}
