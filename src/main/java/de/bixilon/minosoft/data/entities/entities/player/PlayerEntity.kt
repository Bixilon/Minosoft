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
package de.bixilon.minosoft.data.entities.entities.player

import de.bixilon.minosoft.data.Gamemodes
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.player.PlayerProperties
import de.bixilon.minosoft.data.player.PlayerProperty
import de.bixilon.minosoft.data.player.tab.TabListItem
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.nbt.tag.CompoundTag
import glm_.vec3.Vec3

class PlayerEntity(
    connection: PlayConnection,
    entityType: EntityType,
    position: Vec3 = Vec3(0, 0, 0),
    rotation: EntityRotation = EntityRotation(0.0, 0.0),
    name: String = "TBA",
    properties: Map<PlayerProperties, PlayerProperty> = mapOf(),
    var tabListItem: TabListItem = TabListItem(name = name, gamemode = Gamemodes.SURVIVAL, properties = properties),
) : LivingEntity(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Gamemode")
    val gamemode: Gamemodes
        get() = tabListItem.gamemode

    @get:EntityMetaDataFunction(name = "name")
    val name: String
        get() = tabListItem.name

    override val hasCollisions: Boolean
        get() = gamemode != Gamemodes.SPECTATOR

    @get:EntityMetaDataFunction(name = "Absorption hearts")
    val playerAbsorptionHearts: Float
        get() = entityMetaData.sets.getFloat(EntityMetaDataFields.PLAYER_ABSORPTION_HEARTS)

    @get:EntityMetaDataFunction(name = "Score")
    val score: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.PLAYER_SCORE)

    private fun getSkinPartsFlag(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.PLAYER_SKIN_PARTS_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "Main hand")
    override val mainHand: Hands
        get() = if (entityMetaData.sets.getByte(EntityMetaDataFields.PLAYER_SKIN_MAIN_HAND).toInt() == 0x01) Hands.OFF_HAND else Hands.MAIN_HAND

    @get:EntityMetaDataFunction(name = "Left shoulder entity data")
    val leftShoulderData: CompoundTag?
        get() = entityMetaData.sets.getNBT(EntityMetaDataFields.PLAYER_LEFT_SHOULDER_DATA)

    @get:EntityMetaDataFunction(name = "Right shoulder entity data")
    val rightShoulderData: CompoundTag?
        get() = entityMetaData.sets.getNBT(EntityMetaDataFields.PLAYER_RIGHT_SHOULDER_DATA)

    companion object : EntityFactory<PlayerEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("player")
    }
}
