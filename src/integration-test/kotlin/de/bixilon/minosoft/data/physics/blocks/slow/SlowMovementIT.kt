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

package de.bixilon.minosoft.data.physics.blocks.slow

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.SkipException

abstract class SlowMovementIT {

    protected var block: BlockState? = null
        get() {
            return field ?: throw SkipException("block == null")
        }


    protected fun landing(player: LocalPlayerEntity = createPlayer(createConnection(3))): LocalPlayerEntity {
        player.forceTeleport(Vec3d(5.0, 12.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.runTicks(10)

        return player
    }

    protected fun falling(player: LocalPlayerEntity = createPlayer(createConnection(3))): LocalPlayerEntity {
        player.forceTeleport(Vec3d(5.0, 11.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.runTicks(10)

        return player
    }

    protected fun forwardsMovement(player: LocalPlayerEntity = createPlayer(createConnection(3))): LocalPlayerEntity {
        player.forceTeleport(Vec3d(5.0, 10.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.connection.world[Vec3i(5, 9, 5)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)

        return player
    }

    protected fun fallingForwardsMovement(player: LocalPlayerEntity = createPlayer(createConnection(3))): LocalPlayerEntity {
        player.forceTeleport(Vec3d(5.0, 11.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)

        return player
    }

    protected fun sidewaysMovement1(): LocalPlayerEntity {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(5.0, 11.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.input = PlayerMovementInput(left = true)
        player.runTicks(10)

        return player
    }

    protected fun sidewaysMovement2(): LocalPlayerEntity {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(5.0, 11.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.input = PlayerMovementInput(right = true)
        player.runTicks(10)

        return player
    }

    protected fun combinedMovement(): LocalPlayerEntity {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(5.0, 11.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.input = PlayerMovementInput(forward = true, right = true)
        player.runTicks(10)

        return player
    }

    protected fun standing(): LocalPlayerEntity {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(5.0, 10.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.connection.world[Vec3i(5, 9, 5)] = StoneTest0.state
        player.runTicks(10)

        return player
    }

    protected fun standingJump1(): LocalPlayerEntity {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(5.0, 10.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.connection.world[Vec3i(5, 9, 5)] = StoneTest0.state

        player.runTicks(10)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(1)

        return player
    }

    protected fun standingJump2(): LocalPlayerEntity {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(5.0, 10.0, 5.0))
        player.connection.world[Vec3i(5, 10, 5)] = block
        player.connection.world[Vec3i(5, 9, 5)] = StoneTest0.state

        player.runTicks(10)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(3)

        return player
    }
}
