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

package de.bixilon.minosoft.data.physics.input

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.SlabTest0
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import org.testng.Assert
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class SneakIT {

    fun sneak1() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(1)
        player.assertPosition(17.0, 9.0, 8.00588000045985)
        player.assertVelocity(0.0, -0.0784000015258789, 0.005350800572672486)
        player.assertGround(false)
    }

    fun sneak2() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(2)
        player.assertPosition(17.0, 9.0, 8.017110801492372)
        player.assertVelocity(0.0, -0.0784000015258789, 0.010220029234134778)
        player.assertGround()
    }

    fun sneak4() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(4)
        player.assertPosition(17.0, 9.0, 8.107763377843378)
        player.assertVelocity(0.0, -0.0784000015258789, 0.027863772108873714)
        player.assertGround()
    }

    fun sneak8() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(8)
        player.assertPosition(17.0, 9.0, 8.351754765625234)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0346917111076228)
        player.assertGround()
    }

    fun sneak20() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(20)
        player.assertPosition(17.0, 9.0, 9.12738151929247)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03535725486625218)
        player.assertGround()
    }

    fun sneak23() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(23)
        player.assertPosition(17.0, 9.0, 9.271653834465672)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03535764628169529)
        player.assertGround()
    }

    fun sneak24() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(24)
        player.assertPosition(17.0, 9.0, 9.286411484141851)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03535768083008174)
        player.assertGround()
    }

    fun sneak300() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(300)
        player.assertPosition(17.0, 9.0, 9.286411484141851)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03535772237947341)
        player.assertGround()
    }

    fun sneakStepping30() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.connection.world[Vec3i(17, 8, 9)] = SlabTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(30)
        player.assertPosition(17.0, 8.5, 9.721658409737604)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03632423128204995)
        player.assertGround()
    }

    fun sneakSteppingSnow4() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.8))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.connection.world[Vec3i(17, 8, 9)] = player.connection.registries.block[MinecraftBlocks.SNOW]!!.withProperties(BlockProperties.SNOW_LAYERS to 4)
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(20)

        player.assertPosition(17.0, 9.0, 9.294799740816146)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03535725486625218)
        player.assertGround()
    }

    fun sneakSteppingSnow5() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.8))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.connection.world[Vec3i(17, 8, 9)] = player.connection.registries.block[MinecraftBlocks.SNOW]!!.withProperties(BlockProperties.SNOW_LAYERS to 5)
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(15)

        player.assertPosition(17.0, 8.5, 9.5393108028158)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0412509796752613)
        player.assertGround()
    }

    fun sneakStepping70() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.connection.world[Vec3i(17, 8, 9)] = SlabTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(70)
        player.assertPosition(17.0, 8.5, 10.286119530815455)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03535772237950313)
        player.assertGround()
    }

    fun sneakStepping100() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.connection.world[Vec3i(17, 8, 9)] = SlabTest0.state
        player.connection.world[Vec3i(17, 8, 10)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(100)
        player.assertPosition(17.0, 9.0, 11.292730428045934)
        player.assertVelocity(0.0, -0.0784000015258789, 0.035357722379473426)
        player.assertGround()
    }

    fun sneakCollision100() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.connection.world[Vec3i(17, 8, 9)] = SlabTest0.state
        player.connection.world[Vec3i(17, 8, 10)] = StoneTest0.state
        player.connection.world[Vec3i(17, 9, 10)] = StoneTest0.state
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(100)
        player.assertPosition(17.0, 8.5, 9.699999988079071)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun sneakSlab() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.connection.world[Vec3i(17, 8, 9)] = StoneTest0.state
        player.connection.world[Vec3i(17, 10, 9)] = SlabTest0.top
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(25)
        player.assertPosition(17.0, 9.0, 9.451169168366418)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03535769969350293)
        player.assertGround()
    }

    fun unsneakSlab() {
        val player = createPlayer(createConnection(3))
        player.forceTeleport(Vec3d(17.0, 9.0, 8.0))
        player.connection.world[Vec3i(17, 8, 8)] = StoneTest0.state
        player.connection.world[Vec3i(17, 8, 9)] = StoneTest0.state
        player.connection.world[Vec3i(17, 10, 9)] = SlabTest0.top
        player.input = PlayerMovementInput(forward = true, sneak = true)
        player.runTicks(25)
        player.input = PlayerMovementInput()
        player.runTicks(10)
        player.assertPosition(17.0, 9.0, 9.525270446397125)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
        Assert.assertEquals(player.pose, Poses.SNEAKING)
    }
}
