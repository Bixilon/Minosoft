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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.observer.set.SetObserver.Companion.observedSet
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.EquipmentSlots
import de.bixilon.minosoft.data.entities.EntityAnimations
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.GlobalPosition
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.item.items.armor.DyeableArmorItem
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.entity.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entity.models.EntityModel
import de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player.PlayerModel
import de.bixilon.minosoft.gui.rendering.util.VecUtil.clamp
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class PlayerEntity(
    connection: PlayConnection,
    entityType: EntityType,
    data: EntityData,
    position: Vec3d = Vec3d.EMPTY,
    rotation: EntityRotation = EntityRotation(0.0, 0.0),
    name: String = "",
    properties: PlayerProperties? = null,
    val additional: PlayerAdditional = PlayerAdditional(name = name, properties = properties),
) : LivingEntity(connection, entityType, data, position, rotation) {
    protected var _model: PlayerModel?
        get() = super.model.nullCast()
        set(value) {
            super.model = value
        }

    override val dimensions: Vec2
        get() = pose?.let { DIMENSIONS[it] } ?: Vec2(type.width, type.height)

    @get:SynchronizedEntityData
    val gamemode: Gamemodes
        get() = additional.gamemode

    @get:SynchronizedEntityData
    val name: String
        get() = additional.name

    @get:SynchronizedEntityData
    val playerAbsorptionHearts: Float
        get() = data.get(ABSORPTION_HEARTS_DATA, 0.0f)

    @get:SynchronizedEntityData
    val score: Int
        get() = data.get(SCORE_DATA, 0)

    @get:SynchronizedEntityData
    val skinParts: MutableSet<SkinParts> by observedSet(mutableSetOf())


    protected open fun updateSkinParts(flags: Int) {
        for (part in SkinParts.VALUES) {
            if (!flags.isBitMask(part.bitmask)) {
                skinParts -= part
            }
            skinParts += part
        }
    }

    init {
        data.observe(SKIN_PARTS_DATA) { raw: Any? -> updateSkinParts(raw?.toInt() ?: 0) }
    }

    @get:SynchronizedEntityData
    open val mainArm: Arms
        get() = if (data.get(MAIN_ARM_DATA, 0x00.toByte()).toInt() == 0x01) Arms.RIGHT else Arms.LEFT

    @get:SynchronizedEntityData
    val leftShoulderData: JsonObject?
        get() = data.get(LEFT_SHOULDER_DATA_DATA, null)

    @get:SynchronizedEntityData
    val rightShoulderData: JsonObject?
        get() = data.get(RIGHT_SHOULDER_DATA_DATA, null)

    @get:SynchronizedEntityData
    val lastDeathPosition: GlobalPosition?
        get() = data.get(LAST_DEATH_POSITION_DATA, null)

    override val spawnSprintingParticles: Boolean
        get() = super.spawnSprintingParticles && gamemode != Gamemodes.SPECTATOR

    override fun tick() {
        if (gamemode == Gamemodes.SPECTATOR) {
            onGround = false
        }
        // ToDo: Update water submersion state
        super.tick()

        val clampedPosition = position.clamp(-World.MAX_SIZEd, World.MAX_SIZEd)
        if (clampedPosition != position) {
            position = clampedPosition
        }
    }

    override val hitboxColor: RGBColor
        get() {
            if (this.isInvisible) {
                return ChatColors.GREEN
            }
            val chestPlate = equipment[EquipmentSlots.CHEST]
            if (chestPlate != null && chestPlate.item.item is DyeableArmorItem) {
                chestPlate._display?.dyeColor?.let { return it }
            }
            val formattingCode = additional.team?.formattingCode
            if (formattingCode is RGBColor) {
                return formattingCode
            }
            return ChatColors.RED
        }

    override fun createModel(renderer: EntityRenderer): EntityModel<PlayerEntity>? {
        return PlayerModel(renderer, this).apply { this@PlayerEntity.model = this }
    }


    fun swingHand(hand: Hands) {
        val arm = hand.getArm(mainArm)
        _model?.swingArm(arm)
    }

    override fun handleAnimation(animation: EntityAnimations) {
        when (animation) {
            EntityAnimations.SWING_MAIN_ARM -> swingHand(Hands.MAIN)
            EntityAnimations.SWING_OFF_ARM -> swingHand(Hands.OFF)
            else -> super.handleAnimation(animation)
        }
    }

    companion object {
        private val ABSORPTION_HEARTS_DATA = EntityDataField("PLAYER_ABSORPTION_HEARTS")
        private val SCORE_DATA = EntityDataField("PLAYER_SCORE")
        private val SKIN_PARTS_DATA = EntityDataField("PLAYER_SKIN_PARTS_FLAGS")
        private val MAIN_ARM_DATA = EntityDataField("PLAYER_SKIN_MAIN_HAND")
        private val LEFT_SHOULDER_DATA_DATA = EntityDataField("PLAYER_LEFT_SHOULDER_DATA")
        private val RIGHT_SHOULDER_DATA_DATA = EntityDataField("PLAYER_RIGHT_SHOULDER_DATA")
        private val LAST_DEATH_POSITION_DATA = EntityDataField("PLAYER_LAST_DEATH_POSITION")

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
