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

package de.bixilon.minosoft.gui.rendering.hud.elements.other

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderBuilder
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.nodes.HUDElement
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.ImageNode
import de.bixilon.minosoft.gui.rendering.hud.nodes.properties.NodeSizing
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

class CrosshairHUDElement(
    hudRender: HUDRenderer,
) : HUDElement(hudRender) {
    private lateinit var crosshairImage: ImageNode

    override fun init() {
        val atlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:crosshair")]!!
        crosshairImage = ImageNode(hudRenderer.renderWindow, NodeSizing(minSize = atlasElement.binding.size), textureLike = atlasElement)
        layout.addChild(Vec2i(0, 0), crosshairImage)
    }

    companion object : HUDRenderBuilder<CrosshairHUDElement> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:crosshair")
        override val DEFAULT_PROPERTIES = HUDElementProperties(
            position = Vec2(0.0f, 0.0f),
            xBinding = HUDElementProperties.PositionBindings.CENTER,
            yBinding = HUDElementProperties.PositionBindings.CENTER,
        )

        override fun build(hudRenderer: HUDRenderer): CrosshairHUDElement {
            return CrosshairHUDElement(hudRenderer)
        }
    }
}
