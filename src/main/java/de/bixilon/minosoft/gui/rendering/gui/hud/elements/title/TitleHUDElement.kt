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

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.gui.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.LayoutedHUDElement
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2i

class TitleHUDElement(hudRenderer: HUDRenderer) : LayoutedHUDElement<TitleElement>(hudRenderer) {
    override val layout: TitleElement = TitleElement(hudRenderer)
    override val layoutOffset: Vec2i
        get() = TODO("Not yet implemented")


    companion object : HUDBuilder<TitleHUDElement> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:title".toResourceLocation()

        override fun build(hudRenderer: HUDRenderer): TitleHUDElement {
            return TitleHUDElement(hudRenderer)
        }
    }

}
