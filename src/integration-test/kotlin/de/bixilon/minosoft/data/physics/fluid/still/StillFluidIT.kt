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

package de.bixilon.minosoft.data.physics.fluid.still

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.SkipException

abstract class StillFluidIT {
    private val walkingConnection by lazy {
        val connection = createConnection(4)
        connection.world.fill(
            Vec3i(-10, 16, -10),
            Vec3i(20, 16, 20),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 0)
        )
        connection.world.fill(Vec3i(-10, 15, -10), Vec3i(20, 15, 20), StoneTest0.state)

        return@lazy connection
    }
    private val poolConnection by lazy {
        val connection = createConnection(4)
        connection.world.fill(
            Vec3i(-10, 16, -10),
            Vec3i(20, 20, 20),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 0)
        )
        connection.world.fill(Vec3i(-10, 15, -10), Vec3i(20, 15, 20), StoneTest0.state)

        return@lazy connection
    }

    protected var block: FluidBlock? = null
        get() {
            return field ?: throw SkipException("block == null")
        }

    protected fun landing1(): LocalPlayerEntity {
        val player = createPlayer(createConnection(3))
        player.connection.world.fill(
            Vec3i(4, 5, 7),
            Vec3i(4, 16, 7),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 0)
        )
        player.forceTeleport(Vec3d(4.0, 18.0, 7.0))
        player.runTicks(10)
        return player
    }

    protected fun landing2(): LocalPlayerEntity {
        val player = createPlayer(createConnection(3))
        player.connection.world.fill(
            Vec3i(4, 5, 7),
            Vec3i(4, 16, 7),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 0)
        )
        player.forceTeleport(Vec3d(4.0, 30.0, 7.0))
        player.runTicks(35)
        return player
    }

    protected fun walking1(): LocalPlayerEntity {
        val player = createPlayer(this.walkingConnection)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)
        return player
    }

    protected fun walking2(): LocalPlayerEntity {
        val player = createPlayer(this.walkingConnection)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.input = PlayerMovementInput(backward = true)
        player.runTicks(48)
        return player
    }

    protected fun walking3(): LocalPlayerEntity {
        val connection = createConnection(4)
        connection.world.fill(
            Vec3i(-10, 16, -10),
            Vec3i(20, 17, 20),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 0)
        )
        connection.world.fill(Vec3i(-10, 15, -10), Vec3i(20, 15, 20), StoneTest0.state)

        val player = createPlayer(connection)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.input = PlayerMovementInput(backward = true)
        player.runTicks(48)
        return player
    }

    protected fun sprinting1(): LocalPlayerEntity {
        val player = createPlayer(this.walkingConnection)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.input = PlayerMovementInput(forward = true, left = true, sprint = true)
        player.runTicks(15)
        return player
    }

    protected fun jumping1(): LocalPlayerEntity {
        val player = createPlayer(this.walkingConnection)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(5)
        return player
    }

    protected fun sinking1(): LocalPlayerEntity {
        val player = createPlayer(this.poolConnection)
        player.forceTeleport(Vec3d(4.0, 20.0, 7.0))
        player.runTicks(15)
        return player
    }

    protected fun sinking2(): LocalPlayerEntity {
        val player = createPlayer(this.poolConnection)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.runTicks(28)
        return player
    }

    protected fun swimUpwards1(): LocalPlayerEntity {
        val player = createPlayer(this.poolConnection)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(28)
        return player
    }

    protected fun swimUpwards2(): LocalPlayerEntity {
        val player = createPlayer(this.poolConnection)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.input = PlayerMovementInput(forward = true, jump = true)
        player.runTicks(28)
        return player
    }

    protected fun swimUpwards3(): LocalPlayerEntity {
        val player = createPlayer(this.poolConnection)
        player.forceTeleport(Vec3d(4.0, 20.0, 7.0))
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(28)
        return player
    }

    protected fun swimUpwards4(): LocalPlayerEntity {
        val player = createPlayer(this.poolConnection)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(10)
        return player
    }

    protected fun knockDownwards(): LocalPlayerEntity {
        val player = createPlayer(this.poolConnection)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.input = PlayerMovementInput(sneak = true)
        player.runTicks(3)
        return player
    }

    protected fun knockDownwards2(): LocalPlayerEntity {
        val player = createPlayer(this.poolConnection)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.input = PlayerMovementInput(sneak = true)
        player.runTicks(9)
        return player
    }

    protected fun knockDownwards3(): LocalPlayerEntity {
        val player = createPlayer(this.poolConnection)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.input = PlayerMovementInput(sneak = true)
        player.runTicks(19)
        return player
    }
}
