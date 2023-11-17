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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.observer.set.SetObserver.Companion.observedSet
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.entities.EntityAnimations
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.GlobalPosition
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.item.items.dye.DyeableItem
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.player.PlayerRenderer
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.physics.entities.living.player.PlayerPhysics
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W12A
import java.util.*

abstract class PlayerEntity(
    connection: PlayConnection,
    entityType: EntityType,
    data: EntityData,
    position: Vec3d = Vec3d.EMPTY,
    rotation: EntityRotation = EntityRotation.EMPTY,
    val additional: PlayerAdditional,
) : LivingEntity(connection, entityType, data, position, rotation) {

    override val dimensions: Vec2
        get() = pose?.let { getDimensions(it) } ?: Vec2(type.width, type.height)

    override fun getDimensions(pose: Poses): Vec2? {
        if (pose == Poses.SNEAKING) {
            return if (connection.version < V_19W12A) SNEAKING_LEGACY else SNEAKING
        }
        return DIMENSIONS[pose]
    }

    override val heightOffset: Double get() = -0.35

    override val canRaycast: Boolean get() = super.canRaycast && gamemode != Gamemodes.SPECTATOR

    @get:SynchronizedEntityData
    val gamemode: Gamemodes
        get() = additional.gamemode

    @get:SynchronizedEntityData
    val playerAbsorptionHearts: Float
        get() = data.get(ABSORPTION_HEARTS_DATA, 0.0f)

    @get:SynchronizedEntityData
    val score: Int
        get() = data.get(SCORE_DATA, 0)

    protected var aabbPose = pose
    override var defaultAABB: AABB = createDefaultAABB()
        get() {
            val pose = pose
            if (aabbPose == pose) return field
            field = createDefaultAABB()
            aabbPose = pose
            return field
        }

    val skinParts: MutableSet<SkinParts> by observedSet(SkinParts.set())

    override val isNameVisible get() = true
    override val name: ChatComponent? get() = additional.tabDisplayName // minecraft does use the plain name

    protected open fun updateSkinParts(flags: Int) {
        for (part in SkinParts.VALUES) {
            if (flags.isBitMask(part.bitmask)) {
                skinParts += part
            } else {
                skinParts -= part
            }
        }
    }

    init {
        data.observe(SKIN_PARTS_DATA) { raw: Any? -> updateSkinParts(raw?.toInt() ?: 0) }
    }

    private var _mainArm by data(MAIN_ARM_DATA, 0x01)

    @get:SynchronizedEntityData
    open val mainArm: Arms
        get() = if (_mainArm == 0x01) Arms.RIGHT else Arms.LEFT

    @get:SynchronizedEntityData
    val leftShoulderData: JsonObject?
        get() = data.get(LEFT_SHOULDER_DATA_DATA, null)

    @get:SynchronizedEntityData
    val rightShoulderData: JsonObject?
        get() = data.get(RIGHT_SHOULDER_DATA_DATA, null)

    @get:SynchronizedEntityData
    val lastDeathPosition: GlobalPosition?
        get() = data.get(LAST_DEATH_POSITION_DATA, null)

    override val hitboxColor: RGBColor
        get() {
            if (this.isInvisible) {
                return ChatColors.GREEN
            }
            val chestPlate = equipment[EquipmentSlots.CHEST]
            if (chestPlate != null && chestPlate.item.item is DyeableItem) {
                chestPlate._display?.dyeColor?.let { return it }
            }
            additional.team?.formatting?.color?.let { return it }
            return ChatColors.RED
        }

    override fun createPhysics(): PlayerPhysics<*> = PlayerPhysics(this)

    fun swingHand(hand: Hands) {
        val arm = hand.getArm(mainArm)
        renderer?.nullCast<PlayerRenderer<*>>()?.model?.arm?.swing(arm)
    }

    override fun handleAnimation(animation: EntityAnimations) {
        when (animation) {
            EntityAnimations.SWING_MAIN_ARM -> swingHand(Hands.MAIN)
            EntityAnimations.SWING_OFF_ARM -> swingHand(Hands.OFF)
            else -> super.handleAnimation(animation)
        }
    }

    override fun isInvisible(camera: Entity): Boolean {
        if (!super.isInvisible(camera)) return false
        if (camera !is PlayerEntity) return true
        val team = additional.team ?: return true
        if (team != camera.additional.team) return true
        return !team.visibility.invisibleTeam
    }

    companion object : Identified {
        override val identifier = minecraft("player")

        private val ABSORPTION_HEARTS_DATA = EntityDataField("PLAYER_ABSORPTION_HEARTS")
        private val SCORE_DATA = EntityDataField("PLAYER_SCORE")
        private val SKIN_PARTS_DATA = EntityDataField("PLAYER_SKIN_PARTS_FLAGS")
        private val MAIN_ARM_DATA = EntityDataField("PLAYER_SKIN_MAIN_HAND")
        private val LEFT_SHOULDER_DATA_DATA = EntityDataField("PLAYER_LEFT_SHOULDER_DATA")
        private val RIGHT_SHOULDER_DATA_DATA = EntityDataField("PLAYER_RIGHT_SHOULDER_DATA")
        private val LAST_DEATH_POSITION_DATA = EntityDataField("PLAYER_LAST_DEATH_POSITION")

        private val SNEAKING = Vec2(0.6f, 1.5f)
        private val SNEAKING_LEGACY = Vec2(0.6f, 1.65f)

        private val DIMENSIONS: Map<Poses, Vec2> = EnumMap(mapOf(
            Poses.STANDING to Vec2(0.6f, 1.8f),
            Poses.SLEEPING to Vec2(0.2f, 0.2f),
            Poses.ELYTRA_FLYING to Vec2(0.6f, 0.6f),
            Poses.SWIMMING to Vec2(0.6f, 0.6f),
            Poses.SPIN_ATTACK to Vec2(0.6f, 0.6f),
            Poses.DYING to Vec2(0.2f, 0.2f),
        ))
    }
}
