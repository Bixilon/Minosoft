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
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.physics.entities.living.player.local.LocalPlayerPhysics
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.GroundChangeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.RotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.vehicle.MoveVehicleC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.move.vehicle.VehicleInputC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.player.ToggleFlyC2SP

class MovementPacketSender(
    private val physics: LocalPlayerPhysics,
) : Tickable {
    private val player = physics.entity
    private val connection = player.connection

    var flying = false
    private var sprinting = false
    private var sneaking = false

    private var position = Vec3d.EMPTY
    private var rotation = EntityRotation.EMPTY
    private var onGround = false

    private var lastPacket = 0


    private fun sendSprinting(sprinting: Boolean) {
        if (this.sprinting == sprinting) {
            return
        }
        connection.sendPacket(EntityActionC2SP(player, connection, sprinting.decide(EntityActionC2SP.EntityActions.START_SPRINTING, EntityActionC2SP.EntityActions.STOP_SPRINTING)))
        this.sprinting = sprinting
    }

    private fun sendSneaking(sneaking: Boolean) {
        if (this.sneaking == sneaking) {
            return
        }
        connection.sendPacket(EntityActionC2SP(player, connection, sneaking.decide(EntityActionC2SP.EntityActions.START_SNEAKING, EntityActionC2SP.EntityActions.STOP_SNEAKING)))
        this.sneaking = sneaking
    }


    fun sendFly() {
        val abilities = player.abilities
        val flying = abilities.flying
        if (this.flying == flying) {
            return
        }
        this.flying = flying
        connection.sendPacket(ToggleFlyC2SP(abilities))
    }

    private fun sendMovement(position: Vec3d, rotation: EntityRotation, onGround: Boolean) {
        this.lastPacket++

        val positionChange = (position - this.position).length2() > MIN_MOVEMENT_SQUARED
        val sendPosition = positionChange || this.lastPacket >= MAX_POSITION_PACKET_INTERVAL
        val sendRotation = rotation != this.rotation

        val packet = when {
            sendPosition && sendRotation -> PositionRotationC2SP(position, physics.eyeY, rotation, onGround)
            sendPosition -> PositionC2SP(position, physics.eyeY, onGround)
            sendRotation -> RotationC2SP(rotation, onGround)
            onGround != this.onGround -> GroundChangeC2SP(onGround)
            else -> null
        }
        packet?.let { connection.sendPacket(it) }

        if (sendPosition) {
            this.lastPacket = 0
            if (positionChange) {
                this.position = position
            }
        }
        if (sendRotation) {
            this.rotation = rotation
        }
        this.onGround = onGround
    }

    private fun sendAll() {
        sendSprinting(player.isSprinting)
        sendSneaking(player.isSneaking)

        if (connection.camera.entity == connection.player) {
            sendMovement(player.physics.position, player.physics.rotation, player.physics.onGround)
        }
    }

    private fun sendVehicle(vehicle: Entity) {
        connection.sendPacket(RotationC2SP(player.physics.rotation, player.physics.onGround))
        connection.sendPacket(VehicleInputC2SP(physics.input.sideways, physics.input.forwards, player.input.jump, player.input.sneak))
        if (vehicle == player || !vehicle.clientControlled) {
            return
        }
        connection.sendPacket(MoveVehicleC2SP(vehicle.physics.position, vehicle.physics.rotation))
        sendSprinting(player.isSprinting)
    }

    fun sendPositionRotation() {
        connection.sendPacket(PositionRotationC2SP(physics.position, physics.eyeY, physics.rotation, physics.onGround))
    }

    override fun tick() {
        val vehicle = player.attachment.getRootVehicle()
        if (vehicle != null) {
            sendVehicle(vehicle)
        } else {
            sendAll()
        }
    }

    companion object {
        const val MIN_MOVEMENT_SQUARED = 0.0002
        const val MAX_POSITION_PACKET_INTERVAL = 20
    }
}
