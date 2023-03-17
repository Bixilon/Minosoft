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

package de.bixilon.minosoft.camera.target

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.registries.blocks.DirtTest0
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["raycasting"], dependsOnGroups = ["block"])
class TargetHandlerTest {

    fun first() {
        val connection = createConnection(2)
        val player = createPlayer()
        connection.camera::entity.forceSet(DataObserver(player))
        player.physics.forceTeleport(Vec3d(7.7, 69.0, 5.7))
        player.physics.forceSetRotation(EntityRotation(-34.8f, 52.6f)) // 0.2963716, -0.80799085, 0.5092297

        connection.world[7, 68, 5] = StoneTest0.state
        connection.world[8, 69, 5] = StoneTest0.state
        connection.world[7, 69, 6] = DirtTest0.state
        connection.world[8, 69, 6] = StoneTest0.state

        player.renderInfo::eyePosition.forceSet(player.physics.position.toVec3 + Vec3(0.0, 1.5, 0.0))
        player.renderInfo::rotation.forceSet(player.physics.rotation)

        connection.camera.target.update()
        val target = connection.camera.target.target

        assertNotNull(target)
        assertTrue(target is BlockTarget)
        target as BlockTarget
        assertEquals(target.blockPosition, Vec3i(7, 69, 6))
        assertEquals(target.state, DirtTest0.state)
        assertEquals(target.direction, Directions.UP)
        assertEquals(target.cursor.y, 1.0)
    }

    fun front() {
        val connection = createConnection(2)
        val player = createPlayer()
        connection.camera::entity.forceSet(DataObserver(player))
        player.physics.forceTeleport(Vec3d(9.3, 69.0, 2.59))
        player.physics.forceSetRotation(EntityRotation(89.0f, 8.1f))

        connection.world[8, 70, 2] = StoneTest0.state

        player.renderInfo::eyePosition.forceSet(player.physics.position.toVec3 + Vec3(0.0, 1.5, 0.0))
        player.renderInfo::rotation.forceSet(player.physics.rotation)

        connection.camera.target.update()
        val target = connection.camera.target.target

        assertNotNull(target)
        assertTrue(target is BlockTarget)
        target as BlockTarget
        assertEquals(target.blockPosition, Vec3i(8, 70, 2))
        assertEquals(target.state, StoneTest0.state)
        assertEquals(target.direction, Directions.EAST)
        assertEquals(target.cursor.x, 1.0)
    }

    fun inBlock() {
        val connection = createConnection(2)
        val player = createPlayer()
        connection.camera::entity.forceSet(DataObserver(player))
        player.physics.forceTeleport(Vec3d(9.3, 69.0, 2.59))
        player.physics.forceSetRotation(EntityRotation(89.0f, 8.1f))

        connection.world[9, 70, 2] = StoneTest0.state

        player.renderInfo::eyePosition.forceSet(player.physics.position.toVec3 + Vec3(0.0, 1.5, 0.0))
        player.renderInfo::rotation.forceSet(player.physics.rotation)

        connection.camera.target.update()
        val target = connection.camera.target.target

        assertNotNull(target)
        assertTrue(target is BlockTarget)
        target as BlockTarget
        assertEquals(target.blockPosition, Vec3i(9, 70, 2))
        assertEquals(target.state, StoneTest0.state)
        assertEquals(target.direction, Directions.WEST)
    }
}
