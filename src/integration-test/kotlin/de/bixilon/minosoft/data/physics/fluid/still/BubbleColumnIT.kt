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
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.physics.PhysicsTestUtil
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.BubbleColumnBlock
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.WorldTestUtil.fill
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class BubbleColumnIT {
    private var block: BubbleColumnBlock = unsafeNull()
    private val drag by lazy { bubbleColumn(5, true) }
    private val noDrag by lazy { bubbleColumn(5, false) }

    private fun bubbleColumn(height: Int, drag: Boolean): PlayConnection {
        val connection = ConnectionTestUtil.createConnection(4)
        connection.world.fill(Vec3i(-10, 16, -10), Vec3i(10, 15 + height, 10), block.withProperties(BlockProperties.BUBBLE_COLUMN_DRAG to drag))
        connection.world.fill(Vec3i(-10, 15, -10), Vec3i(10, 15, 10), StoneTest0.state)

        return connection
    }

    @Test(priority = -1)
    fun getWater() {
        this.block = IT.REGISTRIES.block[BubbleColumnBlock].unsafeCast()
    }

    fun bubbleColumnUp() {
        val player = PhysicsTestUtil.createPlayer(this.noDrag)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.runTicks(4)
        player.assertPosition(4.0, 18.489000022411343, 7.0)
        player.assertVelocity(0.0, 0.715000010728836, 0.0)
    }

    fun bubbleColumnUp2() {
        val player = PhysicsTestUtil.createPlayer(this.noDrag)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.runTicks(10)
        player.assertPosition(4.0, 22.837708143972158, 7.0)
        player.assertVelocity(0.0, 0.5214658887862557, 0.0)
    }

    fun bubbleColumnUpForward() {
        val player = PhysicsTestUtil.createPlayer(this.noDrag)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)
        player.assertPosition(4.0, 22.424268131593465, 7.639423829879718)
        player.assertVelocity(0.0, 0.39853468033746375, 0.08809752838194507)
    }

    fun bubbleColumnUpForward2() {
        val player = PhysicsTestUtil.createPlayer(this.noDrag)
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(17)
        player.assertPosition(4.0, 23.459491105870534, 8.57281202612172)
        player.assertVelocity(0.0, -0.17096977008391884, 0.14129261480287342)
    }

    fun bubbleColumnUpSurface() {
        val player = PhysicsTestUtil.createPlayer(bubbleColumn(1, false))
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.runTicks(10)
        player.assertPosition(4.0, 16.97077463276175, 7.0)
        player.assertVelocity(0.0, 0.3925845144167298, 0.0)
    }

    fun bubbleColumnUpSurface2() {
        val player = PhysicsTestUtil.createPlayer(bubbleColumn(1, false))
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.runTicks(18)
        player.assertPosition(4.0, 17.790628595164694, 7.0)
        player.assertVelocity(0.0, -0.251012561400887, 0.0)
    }

    fun bubbleColumnUpSurfaceForward() {
        val player = PhysicsTestUtil.createPlayer(bubbleColumn(1, false))
        player.forceTeleport(Vec3d(4.0, 17.0, 7.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(18)
        player.assertPosition(4.0, 17.790628595164694, 9.12125927324846)
        player.assertVelocity(0.0, -0.251012561400887, 0.16188672002063723)
    }

    fun bubbleColumnStartSwimming() {
        val player = PhysicsTestUtil.createPlayer(this.drag)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(10)
        player.assertPosition(4.0, 16.937760820109034, 7.799062161009752)
        player.assertVelocity(0.0, -0.24000000357627868, 0.11413376498826718)
        assertEquals(player.pose, Poses.SWIMMING)
    }

    fun bubbleColumnContinueSwimming() {
        val player = PhysicsTestUtil.createPlayer(this.drag)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.input = PlayerMovementInput(forward = true, sprint = true)
        player.runTicks(10)
        player.input = PlayerMovementInput()
        player.runTicks(1)
        player.assertPosition(4.0, 16.712160816747332, 7.91319592599802)
        player.assertVelocity(0.0, -0.24500000357627869, 0.09130701335119425)
        assertEquals(player.pose, Poses.SWIMMING)
    }

    fun bubbleColumnDown() {
        val player = PhysicsTestUtil.createPlayer(this.drag)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.runTicks(4)
        player.assertPosition(4.0, 18.216999988555905, 7.0)
        player.assertVelocity(0.0, -0.29300000429153444, 0.0)
    }

    fun bubbleColumnDown2() {
        val player = PhysicsTestUtil.createPlayer(this.drag)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.runTicks(10)
        player.assertPosition(4.0, 16.69899996638297, 7.0)
        player.assertVelocity(0.0, -0.24500000357627869, 0.0)
    }

    fun bubbleColumnDown3() {
        val player = PhysicsTestUtil.createPlayer(this.drag)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.runTicks(20)
        player.assertPosition(4.0, 16.0, 7.0)
        player.assertVelocity(0.0, -0.19700000286102295, 0.0)
        player.assertGround()
    }

    fun bubbleColumnDown4() {
        val player = PhysicsTestUtil.createPlayer(this.drag)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.runTicks(20)
        player.assertPosition(4.0, 16.0, 7.0)
        player.assertVelocity(0.0, -0.19700000286102295, 0.0)
        player.assertGround()
    }

    fun bubbleColumnDownForward() {
        val player = PhysicsTestUtil.createPlayer(this.drag)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(10)
        player.assertPosition(4.0, 16.69899996638297, 7.630090695438521)
        player.assertVelocity(0.0, -0.24500000357627869, 0.06998186785731764)
        player.assertGround(false)
    }

    fun bubbleColumnDownForward2() {
        val player = PhysicsTestUtil.createPlayer(this.drag)
        player.forceTeleport(Vec3d(4.0, 19.0, 7.0))
        player.input = PlayerMovementInput(forward = true)
        player.runTicks(19)
        player.assertPosition(4.0, 16.0, 8.475649371879049)
        player.assertVelocity(0.0, -0.10100000143051148, 0.07727014213943671)
        player.assertGround()
    }
}
