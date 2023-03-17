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

package de.bixilon.minosoft.data.physics.blocks.climbing

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.physics.parts.climbing.ClimbingPhysics
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.tags.MinecraftTagTypes
import de.bixilon.minosoft.tags.Tag
import de.bixilon.minosoft.tags.TagList
import de.bixilon.minosoft.tags.TagManager
import org.testng.SkipException

abstract class ClimbingIT {

    protected var block: BlockState? = null
        get() {
            return field ?: throw SkipException("block == null")
        }

    protected fun createConnection(): PlayConnection {
        val connection = ConnectionTestUtil.createConnection(3)
        connection.tags = TagManager(mapOf(MinecraftTagTypes.BLOCK to TagList(mapOf(ClimbingPhysics.TAG to Tag(setOf(block!!.block))))))

        return connection
    }


    protected fun fallingInto1(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 15.0, 5.0))

        player.runTicks(20)

        return player
    }

    protected fun fallingInto2(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 15.0, 5.0))
        player.input = PlayerMovementInput(sneak = true)

        player.runTicks(20)

        return player
    }

    protected fun fallingInto3(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 15.0, 5.0))
        player.input = PlayerMovementInput(jump = true)

        player.runTicks(20)

        return player
    }

    protected fun walkingForwards1(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(forward = true)

        player.runTicks(20)

        return player
    }

    protected fun walkingForwards2(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 4.0, 5.0))
        player.input = PlayerMovementInput(forward = true)

        player.runTicks(29)

        return player
    }

    protected fun walkingSideways1(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(left = true)

        player.runTicks(20)

        return player
    }

    protected fun walkingCombined1(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(forward = true, left = true)

        player.runTicks(20)

        return player
    }

    protected fun sneaking1(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(sneak = true)

        player.runTicks(20)

        return player
    }

    protected fun sneaking2(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(sneak = true)

        player.runTicks(26)

        return player
    }

    protected fun jumping1(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(jump = true)

        player.runTicks(20)

        return player
    }

    protected fun jumping2(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(jump = true)

        player.runTicks(29)

        return player
    }

    protected fun jumpingForwards1(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(forward = true, jump = true)

        player.runTicks(20)

        return player
    }

    protected fun jumpingForwards2(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(forward = true, jump = true)

        player.runTicks(25)

        return player
    }

    protected fun climbingDown1(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))

        player.runTicks(20)

        return player
    }

    protected fun climbingDown2(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))

        player.runTicks(27)

        return player
    }

    protected fun climbingUpCollision(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.connection.world[Vec3i(5, 10, 5)] = StoneTest0.state
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(jump = true)

        player.runTicks(27)

        return player
    }

    protected fun climbingUpTrapdoor(): LocalPlayerEntity {
        val player = createPlayer(createConnection())
        player.connection.world.fill(Vec3i(5, 0, 5), Vec3i(5, 10, 5), block)
        player.connection.world[Vec3i(5, 10, 5)] = player.connection.registries.block[MinecraftBlocks.OAK_TRAPDOOR]?.defaultState?.withProperties(BlockProperties.DOOR_OPEN to true) ?: throw SkipException("Can not get oak trapdoor!")
        player.forceTeleport(Vec3d(5.0, 8.0, 5.0))
        player.input = PlayerMovementInput(jump = true)

        player.runTicks(27)

        return player
    }
}
