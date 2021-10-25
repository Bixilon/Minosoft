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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.title

import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.text.FadingTextElement
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec2.Vec2i

class TitleElement(hudRenderer: HUDRenderer) : Element(hudRenderer) {
    val text = FadingTextElement(hudRenderer, "", scale = 4.0f)
    val subTest = FadingTextElement(hudRenderer, "")
    var fadeInTime = DEFAULT_FADE_IN_TIME
    var stayTime = DEFAULT_STAY_TIME
    var fadeOutTime = DEFAULT_FADE_OUT_TIME


    companion object {
        const val DEFAULT_FADE_IN_TIME = 20L * ProtocolDefinition.TICK_TIME
        const val DEFAULT_STAY_TIME = 60L * ProtocolDefinition.TICK_TIME
        const val DEFAULT_FADE_OUT_TIME = 20L * ProtocolDefinition.TICK_TIME
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        TODO("Not yet implemented")
    }

    override fun forceSilentApply() {
        TODO("Not yet implemented")
    }
}
