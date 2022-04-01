/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.physics.pipeline.parts.ClampPositionPart
import de.bixilon.minosoft.data.physics.pipeline.parts.UpdatePlayerPropertiesPart
import de.bixilon.minosoft.data.player.Arms
import de.bixilon.minosoft.data.player.properties.PlayerProperties
import de.bixilon.minosoft.data.player.tab.TabListItem
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import glm_.vec2.Vec2

abstract class PlayerEntity(
    connection: PlayConnection,
    entityType: EntityType,
    name: String = "<TBA>",
    properties: PlayerProperties = PlayerProperties(),
    var tabListItem: TabListItem = TabListItem(name = name, gamemode = Gamemodes.SURVIVAL, properties = properties),
) : LivingEntity(connection, entityType) {
    override val dimensions: Vec2
        get() {
            val pose = pose ?: return super.dimensions
            if (pose == Poses.SNEAKING) {
                return when {
                    versionId < ProtocolVersions.V_15W42A -> SNEAKING_POSITIONS[0]
                    versionId < ProtocolVersions.V_19W12A -> SNEAKING_POSITIONS[1]
                    else -> SNEAKING_POSITIONS[2]
                }
            }
            return DIMENSIONS[pose] ?: return super.dimensions
        }

    @get:EntityMetaDataFunction(name = "Gamemode")
    val gamemode: Gamemodes
        get() = tabListItem.gamemode

    @get:EntityMetaDataFunction(name = "name")
    val name: String
        get() = tabListItem.name

    @get:EntityMetaDataFunction(name = "Absorption hearts")
    val playerAbsorptionHearts: Float
        get() = data.sets.getFloat(EntityDataFields.PLAYER_ABSORPTION_HEARTS)

    @get:EntityMetaDataFunction(name = "Score")
    val score: Int
        get() = data.sets.getInt(EntityDataFields.PLAYER_SCORE)

    private fun getSkinPartsFlag(bitMask: Int): Boolean {
        return data.sets.getBitMask(EntityDataFields.PLAYER_SKIN_PARTS_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "Main arm")
    open val mainArm: Arms
        get() = if (data.sets.getByte(EntityDataFields.PLAYER_SKIN_MAIN_HAND).toInt() == 0x01) Arms.RIGHT else Arms.LEFT

    @get:EntityMetaDataFunction(name = "Left shoulder entity data")
    val leftShoulderData: Map<String, Any>?
        get() = data.sets.getNBT(EntityDataFields.PLAYER_LEFT_SHOULDER_DATA)

    @get:EntityMetaDataFunction(name = "Right shoulder entity data")
    val rightShoulderData: Map<String, Any>?
        get() = data.sets.getNBT(EntityDataFields.PLAYER_RIGHT_SHOULDER_DATA)

    var currentBiome: Biome? = null

    override val isImmobile: Boolean get() = super.isImmobile || isSleeping

    override fun initPipeline() {
        super.initPipeline()
        // ToDo: Update water state
        physics.pipeline.addLast(ClampPositionPart)
        physics.pipeline.addLast(UpdatePlayerPropertiesPart)
        // ToDo: Update pose
    }

    companion object {
        private const val WIDTH = 0.6f
        private val DIMENSIONS: Map<Poses, Vec2> = mapOf(
            Poses.STANDING to Vec2(WIDTH, 1.8f),
            Poses.SLEEPING to Vec2(0.2f, 0.2f),
            Poses.ELYTRA_FLYING to Vec2(WIDTH, WIDTH),
            Poses.SWIMMING to Vec2(WIDTH, WIDTH),
            Poses.SPIN_ATTACK to Vec2(WIDTH, WIDTH),
            Poses.DYING to Vec2(0.2f, 0.2f),
        )
        private val SNEAKING_POSITIONS = listOf(
            Vec2(WIDTH, 1.8f),
            Vec2(WIDTH, 1.65f),
            Vec2(WIDTH, 1.5f),
        )
    }
}
