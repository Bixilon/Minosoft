/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.physics.blocks.bouncing

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.SlabTest0
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import org.testng.SkipException

abstract class BounceIT {

    protected var block: BlockState? = null
        get() {
            return field ?: throw SkipException("block == null")
        }

    protected fun landing(): LocalPlayerEntity {
        val player = createPlayer(createSession(2))
        player.forceTeleport(Vec3d(5.0, 12.0, 5.0))
        player.session.world[BlockPosition(5, 10, 5)] = block
        player.runTicks(10)

        return player
    }

    protected fun longFall(): LocalPlayerEntity {
        val player = createPlayer(createSession(2))
        player.forceTeleport(Vec3d(5.0, 12.0, 5.0))
        player.physics.velocity.put(Vec3d(0.0, -2.0, 0.0))
        player.session.world[BlockPosition(5, 10, 5)] = block
        player.runTicks(10)

        return player
    }

    protected fun stillJump(): LocalPlayerEntity {
        val player = createPlayer(createSession(2))
        player.forceTeleport(Vec3d(5.0, 11.0, 5.0))
        player.session.world[BlockPosition(5, 10, 5)] = block
        player.runTicks(5)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(10)

        return player
    }

    protected fun fallJump(): LocalPlayerEntity {
        val player = createPlayer(createSession(2))
        player.forceTeleport(Vec3d(5.0, 12.0, 5.0))
        player.session.world[BlockPosition(5, 10, 5)] = block
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(30)

        return player
    }

    protected fun slabJump(): LocalPlayerEntity {
        val player = createPlayer(createSession(2))
        player.forceTeleport(Vec3d(5.0, 11.6, 5.0))
        player.session.world[BlockPosition(5, 10, 5)] = block
        player.session.world[BlockPosition(5, 11, 5)] = SlabTest0.state
        player.runTicks(5)
        player.input = PlayerMovementInput(jump = true)
        player.runTicks(10)

        return player
    }
}
