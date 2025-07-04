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

package de.bixilon.minosoft.camera.target

import glm_.vec3.Vec3d
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.test.IT
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["raycasting"])
class TargetHandlerTest {

    fun first() {
        val session = createSession(2)
        val player = createPlayer()
        session.camera::entity.forceSet(DataObserver(player))
        player.physics.forceTeleport(Vec3d(7.7, 69.0, 5.7))
        player.physics.forceSetRotation(EntityRotation(-34.8f, 52.6f)) // 0.2963716, -0.80799085, 0.5092297

        session.world[7, 68, 5] = IT.BLOCK_1
        session.world[8, 69, 5] = IT.BLOCK_1
        session.world[7, 69, 6] = IT.BLOCK_2
        session.world[8, 69, 6] = IT.BLOCK_1

        player.renderInfo::eyePosition.forceSet(player.physics.position + Vec3d(0.0, 1.5, 0.0))
        player.renderInfo::rotation.forceSet(player.physics.rotation)

        session.camera.target.update()
        val target = session.camera.target.target

        assertNotNull(target)
        assertTrue(target is BlockTarget)
        target as BlockTarget
        assertEquals(target.blockPosition, BlockPosition(7, 69, 6))
        assertEquals(target.state, IT.BLOCK_2)
        assertEquals(target.direction, Directions.UP)
        assertEquals(target.cursor.y, 1.0)
    }

    fun front() {
        val session = createSession(2)
        val player = createPlayer()
        session.camera::entity.forceSet(DataObserver(player))
        player.physics.forceTeleport(Vec3d(9.3, 69.0, 2.59))
        player.physics.forceSetRotation(EntityRotation(89.0f, 8.1f))

        session.world[8, 70, 2] = IT.BLOCK_1

        player.renderInfo::eyePosition.forceSet(player.physics.position + Vec3d(0.0, 1.5, 0.0))
        player.renderInfo::rotation.forceSet(player.physics.rotation)

        session.camera.target.update()
        val target = session.camera.target.target

        assertNotNull(target)
        assertTrue(target is BlockTarget)
        target as BlockTarget
        assertEquals(target.blockPosition, BlockPosition(8, 70, 2))
        assertEquals(target.state, IT.BLOCK_1)
        assertEquals(target.direction, Directions.EAST)
        assertEquals(target.cursor.x, 1.0)
    }

    fun inBlock() {
        val session = createSession(2)
        val player = createPlayer()
        session.camera::entity.forceSet(DataObserver(player))
        player.physics.forceTeleport(Vec3d(9.3, 69.0, 2.59))
        player.physics.forceSetRotation(EntityRotation(89.0f, 8.1f))

        session.world[9, 70, 2] = IT.BLOCK_1

        player.renderInfo::eyePosition.forceSet(player.physics.position + Vec3d(0.0, 1.5, 0.0))
        player.renderInfo::rotation.forceSet(player.physics.rotation)

        session.camera.target.update()
        val target = session.camera.target.target

        assertNotNull(target)
        assertTrue(target is BlockTarget)
        target as BlockTarget
        assertEquals(target.blockPosition, BlockPosition(9, 70, 2))
        assertEquals(target.state, IT.BLOCK_1)
        assertEquals(target.direction, Directions.WEST)
    }
}
