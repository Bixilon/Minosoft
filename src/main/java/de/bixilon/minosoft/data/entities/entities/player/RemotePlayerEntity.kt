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

package de.bixilon.minosoft.data.entities.entities.player

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.player.properties.PlayerProperties
import de.bixilon.minosoft.data.player.tab.TabListItem
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3d

class RemotePlayerEntity(
    connection: PlayConnection,
    entityType: EntityType,
    position: Vec3d = Vec3d.EMPTY,
    rotation: EntityRotation = EntityRotation(0.0, 0.0),
    name: String = "TBA",
    properties: PlayerProperties = PlayerProperties(),
    tabListItem: TabListItem = TabListItem(name = name, gamemode = Gamemodes.SURVIVAL, properties = properties),
) : PlayerEntity(connection, entityType, position, rotation, name, properties, tabListItem) {

    companion object : EntityFactory<PlayerEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:player")
    }
}
