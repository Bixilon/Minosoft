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

package de.bixilon.minosoft.data.physics.blocks.walking

import de.bixilon.kotlinglm.GLM.PIf
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.SlabTest0
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import org.testng.SkipException

abstract class WalkIT {

    protected var block: BlockState? = null
        get() {
            return field ?: throw SkipException("block == null")
        }

    protected open fun createPlayer(connection: PlayConnection): LocalPlayerEntity = PhysicsTestUtil.createPlayer(connection)


    private fun createConnection(): PlayConnection {
        val connection = createConnection(5)
        connection.world.fill(Vec3i(-20, 0, -20), Vec3i(20, 0, 20), block)

        return connection
    }

    private val connection by lazy { createConnection() }

    protected fun landing(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(6.0, 5.0, 6.0))
        player.runTicks(15)

        return player
    }

    protected fun walking1(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(6.0, 1.0, 6.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(2)

        return player
    }

    protected fun walking2(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(6.0, 1.0, 6.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)

        return player
    }

    protected fun walking3(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(6.0, 1.0, 6.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(50)

        return player
    }

    protected fun walking4(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-6.0, 1.0, -6.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)

        return player
    }

    protected fun walking5(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-6.0, 1.0, -6.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(50)

        return player
    }

    protected fun walking6(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-6.0, 1.0, -6.0))
        player.forceRotate(EntityRotation(180.0f, 0.0f))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)

        return player
    }

    protected fun walking7(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-6.0, 1.0, -6.0))
        player.forceRotate(EntityRotation(180.0f, 0.0f))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(50)

        return player
    }

    protected fun walking8(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-6.0, 1.0, -6.0))
        player.input = PlayerMovementInput(forward = true)

        player.forceRotate(EntityRotation(180.0f, 0.0f))
        player.runTicks(10)
        player.forceRotate(EntityRotation(90.0f, 0.0f))
        player.runTicks(10)
        player.forceRotate(EntityRotation(0.0f, 0.0f))
        player.runTicks(10)
        player.forceRotate(EntityRotation(270.0f, 0.0f))
        player.runTicks(10)

        return player
    }

    protected fun walking9(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-6.0, 1.0, -6.0))
        player.input = PlayerMovementInput(forward = true)

        for (x in 0..100) {
            player.tick()
            player.forceRotate(EntityRotation(kotlin.math.sin(x / PIf / 100.0f) * 400.0f, 0.0f))
        }

        return player
    }

    protected fun sprintJump1(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-6.0, 1.0, -6.0))
        player.input = PlayerMovementInput(sprint = true, jump = true, forward = true)
        player.runTicks(2)

        return player
    }

    protected fun sprintJump2(): LocalPlayerEntity {
        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(-6.0, 1.0, -6.0))
        player.input = PlayerMovementInput(sprint = true, jump = true, forward = true)
        player.runTicks(5)

        return player
    }

    protected fun slabWalk(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(-20, 1, -20), Vec3i(20, 1, 20), SlabTest0.state)
        player.forceTeleport(Vec3d(-6.0, 1.5, -6.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(5)
        player.input = PlayerMovementInput()
        player.runTicks(5)

        return player
    }

    protected fun stoneBlockWalk1(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(-18, 3, 2), Vec3i(-10, 3, 10), StoneTest0.state)
        player.connection.world.fill(Vec3i(-16, 3, 4), Vec3i(-12, 3, 8), block!!)

        player.forceTeleport(Vec3d(-15.653410032035934, 4.0, 8.580128352737571))
        player.forceRotate(EntityRotation(180.29749f, 22.493956f))
        player.runTicks(5)
        player.input = PlayerMovementInput(sprint = true)
        player.runTicks(1)
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(10)

        return player
    }

    protected fun stoneBlockWalk2(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(-18, 3, 2), Vec3i(-10, 3, 10), StoneTest0.state)
        player.connection.world.fill(Vec3i(-16, 3, 4), Vec3i(-12, 3, 8), block!!)

        player.forceTeleport(Vec3d(-15.653410032035934, 4.0, 8.580128352737571))
        player.forceRotate(EntityRotation(180.29749f, 22.493956f))
        player.runTicks(5)
        player.input = PlayerMovementInput(sprint = true)
        player.runTicks(1)
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(5)

        return player
    }
}
