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

package de.bixilon.minosoft.data.physics.riding

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.properties.riding.Saddleable
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class AbstractRidingTest<T : Entity> {

    protected abstract fun constructVehicle(connection: PlayConnection): Entity

    protected open fun saddle(entity: T): Unit = Broken("Saddle me!")

    protected fun PlayConnection.createVehicle(saddle: Boolean = true): Entity {
        val entity = constructVehicle(this)

        if (saddle && entity is Saddleable) {
            saddle(entity.unsafeCast())

        }
        // TODO: saddle

        entity.init()

        return entity
    }

    protected fun tickPassenger(entity: Entity) {
        entity.tickRiding()
        for (passenger in entity.attachment.passengers) {
            tickPassenger(passenger)
        }
    }

    protected fun tick(entity: Entity) {
        entity.forceTick(0L)
        val passengers = entity.attachment.passengers.toSet()

        for (passenger in passengers) {
            tickPassenger(passenger)
        }
    }

    protected fun LocalPlayerEntity.ridingTick(count: Int = 1) {
        for (i in 0 until count) {
            this.attachment.vehicle?.let { tick(it) } ?: tick(this)
        }
    }

    protected fun startRiding(): LocalPlayerEntity {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        val vehicle = player.connection.createVehicle(false)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        return player
    }

    protected fun falling(): LocalPlayerEntity {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        val vehicle = player.connection.createVehicle(true)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        player.ridingTick(10)

        return player
    }

    protected fun walking1(): LocalPlayerEntity {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        val vehicle = player.connection.createVehicle(true)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        player.input = PlayerMovementInput(forward = true)

        player.ridingTick(10)

        return player
    }

    protected fun walking2(): LocalPlayerEntity {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        player.connection.world.fill(Vec3i(-10, 4, -10), Vec3i(10, 4, 10), StoneTest0.state)

        val vehicle = player.connection.createVehicle(true)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        player.input = PlayerMovementInput(forward = true)

        player.ridingTick(15)

        return player
    }

    protected fun walkSideways1(): LocalPlayerEntity {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        player.connection.world.fill(Vec3i(-10, 4, -10), Vec3i(10, 4, 10), StoneTest0.state)
        val vehicle = player.connection.createVehicle(true)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        player.input = PlayerMovementInput(left = true)

        player.ridingTick(12)

        return player
    }

    protected fun walkSideways2(): LocalPlayerEntity {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        player.connection.world.fill(Vec3i(-10, 4, -10), Vec3i(10, 4, 10), StoneTest0.state)
        val vehicle = player.connection.createVehicle(true)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        player.input = PlayerMovementInput(left = true, forward = true)

        player.ridingTick(16)

        return player
    }

    protected fun walkBackwards(): LocalPlayerEntity {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        player.connection.world.fill(Vec3i(-10, 4, -10), Vec3i(10, 4, 10), StoneTest0.state)
        val vehicle = player.connection.createVehicle(true)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        player.input = PlayerMovementInput(backward = true)

        player.ridingTick(11)

        return player
    }

    protected fun walkUnsaddled(): LocalPlayerEntity {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        player.connection.world.fill(Vec3i(-10, 4, -10), Vec3i(10, 4, 10), StoneTest0.state)
        val vehicle = player.connection.createVehicle(false)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        player.input = PlayerMovementInput(forward = true)

        player.ridingTick(18)

        return player
    }

    protected fun jump1(): LocalPlayerEntity {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        player.connection.world.fill(Vec3i(-10, 4, -10), Vec3i(10, 4, 10), StoneTest0.state)
        val vehicle = player.connection.createVehicle(true)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        player.input = PlayerMovementInput(forward = true, jump = true)
        player.ridingTick(18)
        player.input = PlayerMovementInput(forward = true, jump = false)
        player.ridingTick(1)

        return player
    }

    protected fun jump2(): LocalPlayerEntity {
        val player = createPlayer(createConnection(5))
        player.forceTeleport(Vec3d(6.0, 5.0, 4.0))
        player.connection.world.fill(Vec3i(-10, 4, -10), Vec3i(10, 4, 10), StoneTest0.state)
        val vehicle = player.connection.createVehicle(true)
        vehicle.forceTeleport(Vec3d(7.0, 6.0, 5.0))

        player.attachment.vehicle = vehicle

        player.input = PlayerMovementInput(forward = true, jump = true)
        player.ridingTick(18)

        player.input = PlayerMovementInput(forward = true, jump = false)
        player.ridingTick(3)

        return player
    }
}
