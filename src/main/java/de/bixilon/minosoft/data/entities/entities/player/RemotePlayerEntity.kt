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

package de.bixilon.minosoft.data.entities.entities.player

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.minecraft

class RemotePlayerEntity(
    connection: PlayConnection,
    entityType: EntityType,
    data: EntityData,
    position: Vec3d = Vec3d.EMPTY,
    rotation: EntityRotation = EntityRotation.EMPTY,
    name: String = "TBA",
    properties: PlayerProperties? = null,
    tabListItem: PlayerAdditional = PlayerAdditional(name = name, gamemode = Gamemodes.SURVIVAL, properties = properties),
) : PlayerEntity(connection, entityType, data, position, rotation, name, properties, tabListItem) {

    companion object : EntityFactory<PlayerEntity> {
        override val identifier: ResourceLocation = minecraft("player")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): RemotePlayerEntity? {
            throw IllegalAccessError("Can not build player entity!")
        }
    }
}
