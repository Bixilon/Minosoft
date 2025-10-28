/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.item.items.armor.DefendingArmorItem.Companion.getProtection
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas
import de.bixilon.minosoft.gui.rendering.gui.atlas.Atlas.Companion.get
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.gui.mesh.consumer.GuiVertexConsumer

class HotbarProtectionElement(guiRenderer: GUIRenderer) : Element(guiRenderer), Pollable {
    private val atlas = ProtectionAtlas(guiRenderer.atlas[ATLAS])

    init {
        forceSilentApply()
    }

    private var protection = 0.0f

    override fun forceRender(offset: Vec2f, consumer: GuiVertexConsumer, options: GUIVertexOptions?) {
        if (protection <= 0.0f) {
            return
        }

        var protectionLeft = protection

        for (i in 0 until 10) {
            val atlasElement = when {
                protectionLeft < 1.0f -> atlas.empty
                protectionLeft < 2.0f -> atlas.half
                else -> atlas.full
            }

            val image = AtlasImageElement(guiRenderer, atlasElement)

            image.render(offset + Vec2f(i * ARMOR_SIZE.x, 0), consumer, options)

            protectionLeft -= 2.0f
        }
    }

    override fun poll(): Boolean {
        val protection = guiRenderer.context.session.player.equipment.getProtection() // ToDo: Check for equipment change


        if (this.protection == protection) {
            return false
        }

        this.protection = protection

        return true
    }

    override fun forceSilentApply() {
        _size = if (protection <= 0.0f) {
            Vec2f.EMPTY
        } else {
            SIZE
        }
        cacheUpToDate = false
    }

    override fun tick() {
        apply()
    }

    private class ProtectionAtlas(atlas: Atlas?) {
        val empty = atlas["empty"]
        val half = atlas["half"]
        val full = atlas["full"]
    }


    companion object {
        val ATLAS = minecraft("hud/hotbar/protection")
        private val ARMOR_SIZE = Vec2i(8, 9)
        private val SIZE = Vec2f(10 * ARMOR_SIZE.x, ARMOR_SIZE.y)
    }
}
