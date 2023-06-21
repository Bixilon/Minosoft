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

package de.bixilon.minosoft.data.physics.fluid.flowing

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.SkipException
import kotlin.math.abs

abstract class FlowingFluidIT {
    protected val levels by lazy {
        arrayOf(
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 7),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 6),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 5),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 4),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 3),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 2),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 1),
            block!!.states.withProperties(BlockProperties.FLUID_LEVEL to 0),
        )
    }


    protected var block: FluidBlock? = null
        get() {
            return field ?: throw SkipException("block == null")
        }


    protected open fun create(): LocalPlayerEntity {
        return createPlayer(createConnection(3))
    }

    protected fun World.star(x: Int = 4, y: Int = 4, z: Int = 4, start: Int = 7, size: Int = 8) {
        fill(
            Vec3i(x - size - 2, y - 1, z - size - 2),
            Vec3i(x + size + 2, y - 1, z + size + 2),
            StoneTest0.block.states.default
        )

        for (offsetX in -size..size) {
            for (offsetZ in -size..size) {
                val distance = abs(offsetX) + abs(offsetZ)
                val level = start - distance
                if (level < 0) continue

                this[x + offsetX, y, z + offsetZ] = levels[level]
            }
        }
    }

    protected fun starMiddle(): LocalPlayerEntity {
        val player = create()
        player.connection.world.star()
        player.forceTeleport(Vec3d(4.5, 5.0, 4.5))

        player.runTicks(16)

        return player
    }

    protected fun starOffset(): LocalPlayerEntity {
        val player = create()
        player.connection.world.star()
        player.forceTeleport(Vec3d(3.5, 5.0, 3.5))

        player.runTicks(16)

        return player
    }

    protected fun starOffset2(): LocalPlayerEntity {
        val player = create()
        player.connection.world.star()
        player.forceTeleport(Vec3d(3.5, 5.0, 3.5))

        player.runTicks(25)

        return player
    }

    protected fun starOffset3(): LocalPlayerEntity {
        val player = create()
        player.connection.world.star()
        player.forceTeleport(Vec3d(3.5, 5.0, 9.5))

        player.runTicks(25)

        return player
    }

    protected fun startJumping(): LocalPlayerEntity {
        val player = create()
        player.connection.world.star()
        player.forceTeleport(Vec3d(3.5, 5.0, 3.5))
        player.runTicks(10)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(10)

        return player
    }


    protected fun lowJumping(): LocalPlayerEntity {
        val player = create()
        player.connection.world.star()
        player.forceTeleport(Vec3d(3.5, 5.0, 3.5))
        player.runTicks(20)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(5)

        return player
    }

    protected fun lowJumping2(): LocalPlayerEntity {
        val player = create()
        player.connection.world.star()
        player.forceTeleport(Vec3d(3.5, 5.0, 10.0))
        player.runTicks(30)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(5)

        return player
    }
}
