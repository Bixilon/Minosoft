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
package de.bixilon.minosoft.data.entities.entities.player.local

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.container.equipment.EntityEquipment
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.entities.entities.player.compass.CompassPosition
import de.bixilon.minosoft.data.registries.effects.attributes.integrated.IntegratedAttributeModifiers
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.factory.EntityModelFactory
import de.bixilon.minosoft.gui.rendering.entities.renderer.player.LocalPlayerRenderer
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.input.camera.MovementInputActions
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.physics.ItemUsing
import de.bixilon.minosoft.physics.entities.living.player.local.LocalPlayerPhysics
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

class LocalPlayerEntity(
    account: Account,
    connection: PlayConnection,
    val keyManagement: SignatureKeyManagement,
) : PlayerEntity(connection, connection.registries.entityType[identifier]!!, EntityData(connection), Vec3d.EMPTY, EntityRotation.EMPTY, PlayerAdditional(account.username, ping = 0, properties = account.properties)), EntityModelFactory<LocalPlayerEntity> {
    var healthCondition by observed(HealthCondition())
    var experienceCondition by observed(ExperienceCondition())
    var compass by observed(CompassPosition())

    var abilities by observed(Abilities())
    var using: ItemUsing? by observed(null)


    val items = PlayerItemManager(this)
    override val equipment: EntityEquipment = EntityEquipment(this, items.inventory.equipment)

    var input = PlayerMovementInput()
    var inputActions = MovementInputActions()

    override val clientControlled get() = true

    override var pose: Poses? = Poses.STANDING


    override val hasGravity: Boolean
        get() = !abilities.flying

    override val uuid: UUID
        get() = super.uuid ?: connection.account.uuid

    override var isSprinting: Boolean = false
        set(value) {
            if (value == field) {
                return
            }
            attributes -= IntegratedAttributeModifiers.SPRINT_SPEED_BOOST

            if (value) {
                attributes += IntegratedAttributeModifiers.SPRINT_SPEED_BOOST
            }
            field = value
        }

    override val isSneaking: Boolean
        get() = input.sneak

    val reachDistance: Double
        get() = if (gamemode == Gamemodes.CREATIVE) 5.0 else 4.5

    override fun tick() {
        if (connection.world.chunks[physics.positionInfo.chunkPosition] == null) { // TODO: Optimize
            // chunk not loaded, so we don't tick?
            return
        }
        super.tick()
    }

    override fun draw(time: Long) = Unit

    fun _draw(time: Long) {
        super.draw(time)
    }

    override val health: Double
        get() = healthCondition.hp.toDouble()

    override val mainArm: Arms
        get() = connection.profiles.connection.mainArm

    override val usingHand: Hands?
        get() = using?.hand

    override fun createPhysics() = LocalPlayerPhysics(this)
    override fun physics(): LocalPlayerPhysics = super.physics().unsafeCast()
    override fun create(renderer: EntitiesRenderer) = LocalPlayerRenderer(renderer, this)
}
