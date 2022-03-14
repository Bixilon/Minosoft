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

package de.bixilon.minosoft.data.physics.pipeline.parts

import de.bixilon.kutil.math.simple.DoubleMath.square
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.physics.pipeline.PipelineBuilder
import de.bixilon.minosoft.data.physics.pipeline.PipelineContext
import de.bixilon.minosoft.data.physics.pipeline.PipelinePart
import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.GroundChangeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.PositionC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.PositionRotationC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.move.RotationC2SP
import glm_.vec3.Vec3d
import kotlin.reflect.KClass

class LocalPlayerPacketSender : PipelinePart<LocalPlayerEntity> {
    override val name: String get() = LocalPlayerPacketSender.name
    private var sprinting = false
    private var sneaking = false
    private var position = Vec3d.EMPTY
    private var rotation = EntityRotation.EMPTY
    private var onGround = false

    private var lastPacketTicks = 0


    private fun sendSprint(entity: LocalPlayerEntity) {
        val sprinting = entity.isSprinting
        if (sprinting == this.sprinting) {
            return
        }
        entity.connection.sendPacket(EntityActionC2SP(entity, entity.connection, if (sprinting) EntityActionC2SP.EntityActions.START_SPRINTING else EntityActionC2SP.EntityActions.STOP_SPRINTING))
        this.sprinting = sprinting
    }

    private fun sendSneak(entity: LocalPlayerEntity) {
        val sneaking = entity.isSneaking
        if (sneaking == this.sneaking) {
            return
        }
        entity.connection.sendPacket(EntityActionC2SP(entity, entity.connection, if (sneaking) EntityActionC2SP.EntityActions.START_SNEAKING else EntityActionC2SP.EntityActions.STOP_SNEAKING))
        this.sneaking = sneaking
    }

    override fun handle(context: PipelineContext, entity: LocalPlayerEntity) {
        if (entity.connection.profiles.rendering.movement.disablePacketSending) {
            return
        }
        val rotation = entity.physics.positioning.rotation
        val onGround = entity.physics.other.onGround
        if (entity.physics.vehicle.vehicle != null) {
            entity.connection.sendPacket(RotationC2SP(rotation, onGround))
            // ToDo: Send SteerVehicleC2SP
            // ToDo: if can move root vehicle, send MoveVehicleC2SP
        }

        sendSprint(entity)
        sendSneak(entity)

        if (entity.spectatingEntity != null) {
            return
        }

        lastPacketTicks++

        val position = entity.physics.positioning.position
        val sendPosition = (position - this.position).length2() > 2.0E-4.square() || lastPacketTicks > 20
        val sendRotation = rotation.yaw != this.rotation.yaw && rotation.pitch != this.rotation.pitch


        if (sendPosition) {
            if (sendRotation) {
                entity.connection.sendPacket(PositionRotationC2SP(position, rotation, onGround))
                this.rotation = rotation
            } else {
                entity.connection.sendPacket(PositionC2SP(position, onGround))
            }
            this.position = position
        } else if (sendRotation) {
            entity.connection.sendPacket(RotationC2SP(rotation, onGround))
            this.rotation = rotation
        } else if (this.onGround != onGround) {
            entity.connection.sendPacket(GroundChangeC2SP(onGround))
        }
        this.onGround = onGround
    }

    companion object : PipelineBuilder<LocalPlayerEntity, LocalPlayerPacketSender> {
        override val name: String = "local_packet_sender"
        override val entity: KClass<LocalPlayerEntity> = LocalPlayerEntity::class

        override fun build(connection: PlayConnection): LocalPlayerPacketSender {
            return LocalPlayerPacketSender()
        }
    }
}
