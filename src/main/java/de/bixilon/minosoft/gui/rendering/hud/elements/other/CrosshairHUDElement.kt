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

package de.bixilon.minosoft.gui.rendering.hud.elements.other

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.ElementListElement
import de.bixilon.minosoft.gui.rendering.hud.elements.primitive.ImageElement

class CrosshairHUDElement(
    hudRender: HUDRenderer,
) : HUDElement(hudRender) {
    private lateinit var crosshairAtlasElement: HUDAtlasElement

    override fun init() {
        crosshairAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:crosshair")]!!
    }


    override fun prepare(elementList: ElementListElement) {
        elementList.addChild(ImageElement(end = crosshairAtlasElement.binding.size, textureLike = crosshairAtlasElement))
    }
}
