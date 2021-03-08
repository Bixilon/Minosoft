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

package de.bixilon.minosoft.config.config.game.elements

import de.bixilon.minosoft.config.config.game.elements.ElementsNames.DEFAULT_HUD_PROPERTIES
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import glm_.vec2.Vec2

data class ElementsGameConfig(
    val entries: MutableMap<ResourceLocation, HUDElementProperties> = mutableMapOf(),
) {
    init {
        for ((resourceLocation, elementProperties) in DEFAULT_HUD_PROPERTIES) {
            if (!entries.containsKey(resourceLocation)) {
                // add key binding
                entries[resourceLocation] = HUDElementProperties(elementProperties)
            }
        }
    }
}

object ElementsNames {
    val HOTBAR_RESOURCE_LOCATION = ResourceLocation("minosoft:hotbar")
    val CROSSHAIR_RESOURCE_LOCATION = ResourceLocation("minosoft:crosshair")
    val WORLD_DEBUG_SCREEN_RESOURCE_LOCATION = ResourceLocation("minosoft:world_debug_screen")
    val SYSTEM_DEBUG_SCREEN_RESOURCE_LOCATION = ResourceLocation("minosoft:system_debug_screen")


    val DEFAULT_HUD_PROPERTIES: Map<ResourceLocation, HUDElementProperties> = mutableMapOf(
        HOTBAR_RESOURCE_LOCATION to HUDElementProperties(
            position = Vec2(0f, -1.0f),
            xBinding = HUDElementProperties.PositionBindings.CENTER,
            yBinding = HUDElementProperties.PositionBindings.FURTHEST_POINT_AWAY,
        ),
        CROSSHAIR_RESOURCE_LOCATION to HUDElementProperties(
            position = Vec2(0f, 0f),
            xBinding = HUDElementProperties.PositionBindings.CENTER,
            yBinding = HUDElementProperties.PositionBindings.CENTER,
        ),
        WORLD_DEBUG_SCREEN_RESOURCE_LOCATION to HUDElementProperties(
            position = Vec2(-1.0f, 1.0f),
            xBinding = HUDElementProperties.PositionBindings.FURTHEST_POINT_AWAY,
            yBinding = HUDElementProperties.PositionBindings.FURTHEST_POINT_AWAY,
            toggleKeyBinding = ResourceLocation("toggle_debug_screen"),
        ),
        SYSTEM_DEBUG_SCREEN_RESOURCE_LOCATION to HUDElementProperties(
            position = Vec2(1.0f, 1.0f),
            xBinding = HUDElementProperties.PositionBindings.FURTHEST_POINT_AWAY,
            yBinding = HUDElementProperties.PositionBindings.FURTHEST_POINT_AWAY,
            toggleKeyBinding = ResourceLocation("toggle_debug_screen"),
        ),
    )

}
