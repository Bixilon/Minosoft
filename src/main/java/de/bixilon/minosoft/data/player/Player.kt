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
package de.bixilon.minosoft.data.player

import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.mappings.materials.Container
import de.bixilon.minosoft.data.mappings.other.ContainerType
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import glm_.vec3.Vec3i

class Player(
    account: Account,
    connection: PlayConnection,
) {
    val healthCondition = PlayerHealthCondition()
    val experienceCondition = PlayerExperienceCondition()
    var spawnPosition: Vec3i = VecUtil.EMPTY_VEC3I
    val entity: PlayerEntity = PlayerEntity(connection, connection.mapping.entityRegistry[PlayerEntity.RESOURCE_LOCATION]!!, VecUtil.EMPTY_VEC3, EntityRotation(0.0, 0.0), account.username)

    @Deprecated(message = "Will be replaced with some kind of teleport manager, ...")
    var isSpawnConfirmed = false

    val baseAbilities = Abilities()

    val containers: MutableMap<Int, Container> = synchronizedMapOf(
        ProtocolDefinition.PLAYER_INVENTORY_ID to Container(
            ContainerType(
                resourceLocation = "TBO".asResourceLocation()
            ),
        ))
    var selectedHotbarSlot: Int = 0
}
