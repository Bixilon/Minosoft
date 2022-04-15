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
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.player.Arms
import de.bixilon.minosoft.data.player.properties.PlayerProperties
import de.bixilon.minosoft.data.player.tab.TabListItem
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.util.VecUtil.clamp
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec2.Vec2
import glm_.vec3.Vec3d

abstract class PlayerEntity(
    connection: PlayConnection,
    entityType: EntityType,
    position: Vec3d = Vec3d.EMPTY,
    rotation: EntityRotation = EntityRotation(0.0, 0.0),
    name: String = "TBA",
    properties: PlayerProperties = PlayerProperties(),
    var tabListItem: TabListItem = TabListItem(name = name, gamemode = Gamemodes.SURVIVAL, properties = properties),
) : LivingEntity(connection, entityType, position, rotation) {
    override val dimensions: Vec2
        get() = pose?.let { DIMENSIONS[it] } ?: Vec2(type.width, type.height)

    @get:SynchronizedEntityData(name = "Gamemode")
    val gamemode: Gamemodes
        get() = tabListItem.gamemode

    @get:SynchronizedEntityData(name = "name")
    val name: String
        get() = tabListItem.name

    @get:SynchronizedEntityData(name = "Absorption hearts")
    val playerAbsorptionHearts: Float
        get() = data.sets.getFloat(EntityDataFields.PLAYER_ABSORPTION_HEARTS)

    @get:SynchronizedEntityData(name = "Score")
    val score: Int
        get() = data.sets.getInt(EntityDataFields.PLAYER_SCORE)

    private fun getSkinPartsFlag(bitMask: Int): Boolean {
        return data.sets.getBitMask(EntityDataFields.PLAYER_SKIN_PARTS_FLAGS, bitMask)
    }

    @get:SynchronizedEntityData(name = "Main arm")
    open val mainArm: Arms
        get() = if (data.sets.getByte(EntityDataFields.PLAYER_SKIN_MAIN_HAND).toInt() == 0x01) Arms.RIGHT else Arms.LEFT

    @get:SynchronizedEntityData(name = "Left shoulder entity data")
    val leftShoulderData: Map<String, Any>?
        get() = data.sets.getNBT(EntityDataFields.PLAYER_LEFT_SHOULDER_DATA)

    @get:SynchronizedEntityData(name = "Right shoulder entity data")
    val rightShoulderData: Map<String, Any>?
        get() = data.sets.getNBT(EntityDataFields.PLAYER_RIGHT_SHOULDER_DATA)

    override val spawnSprintingParticles: Boolean
        get() = super.spawnSprintingParticles && gamemode != Gamemodes.SPECTATOR

    override fun realTick() {
        if (gamemode == Gamemodes.SPECTATOR) {
            onGround = false
        }
        // ToDo: Update water submersion state
        super.realTick()

        val clampedPosition = position.clamp(-World.MAX_SIZEd, World.MAX_SIZEd)
        if (clampedPosition != position) {
            position = clampedPosition
        }
    }

    companion object {
        private val DIMENSIONS: Map<Poses, Vec2> = mapOf(
            Poses.STANDING to Vec2(0.6f, 1.8f),
            Poses.SLEEPING to Vec2(0.2f, 0.2f),
            Poses.ELYTRA_FLYING to Vec2(0.6f, 0.6f),
            Poses.SWIMMING to Vec2(0.6f, 0.6f),
            Poses.SPIN_ATTACK to Vec2(0.6f, 0.6f),
            Poses.SNEAKING to Vec2(0.6f, 1.5f), // ToDo: This changed at some time
            Poses.DYING to Vec2(0.2f, 0.2f),
        )
    }
}
