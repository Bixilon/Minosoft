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
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class MixedFluidIT {
    private var lava: FluidBlock? = null
    private var water: FluidBlock? = null

    @Test(priority = -1)
    fun getFluids() {
        this.lava = IT.REGISTRIES.block[LavaFluid].unsafeCast()!!
        this.water = IT.REGISTRIES.block[WaterFluid].unsafeCast()!!
    }

    private fun World.fill() {
        this[Vec3i(10, 2, 3)] = lava!!.states.withProperties(BlockProperties.FLUID_LEVEL to 0)
        this[Vec3i(10, 1, 3)] = StoneTest0.state
        this[Vec3i(10, 2, 4)] = water!!.states.withProperties(BlockProperties.FLUID_LEVEL to 0)
        this[Vec3i(10, 1, 4)] = StoneTest0.state
    }

    fun stillLavaAndWater1() {
        val player = createPlayer(createConnection(3))
        player.connection.world.fill()
        player.forceTeleport(Vec3d(10.0, 2.0, 3.8))

        player.runTicks(10)

        player.assertPosition(10.0, 2.0, 3.8)
        player.assertVelocity(0.0, -0.005, 0.0)
        player.assertGround()
    }

    fun stillLavaAndWater2() {
        val player = createPlayer(createConnection(3))
        player.connection.world.fill()
        player.forceTeleport(Vec3d(10.0, 2.0, 3.8))
        player.input = PlayerMovementInput(forward = true)

        player.runTicks(10)

        player.assertPosition(10.0, 2.0, 4.430090695438522)
        player.assertVelocity(0.0, -0.005, 0.06998186785731764)
        player.assertGround()
    }

    fun stillLavaAndLava2() {
        val player = createPlayer(createConnection(3))
        player.connection.world.fill()
        player.connection.world[Vec3i(10, 2, 4)] = lava!!.states.withProperties(BlockProperties.FLUID_LEVEL to 0)
        player.forceTeleport(Vec3d(10.0, 2.0, 3.8))
        player.input = PlayerMovementInput(forward = true)

        player.runTicks(10)

        player.assertPosition(10.0, 2.0, 4.152838280230649)
        player.assertVelocity(0.0, -0.02, 0.019580859318430878)
        player.assertGround()
    }

    fun mixedHeight1() {
        val player = createPlayer(createConnection(3))
        player.connection.world[Vec3i(10, 1, 4)] = StoneTest0.state
        player.connection.world[Vec3i(10, 2, 4)] = lava!!.states.withProperties(BlockProperties.FLUID_LEVEL to 7)
        player.connection.world[Vec3i(10, 3, 4)] = water!!.states.withProperties(BlockProperties.FLUID_LEVEL to 7)
        player.forceTeleport(Vec3d(10.0, 2.0, 3.8))
        player.runTicks(2)
        player.input = PlayerMovementInput(jump = true, forward = true)
        player.runTicks(2)

        player.assertPosition(10.0, 2.790999980509281, 3.854880000075102)
        player.assertVelocity(0.0, 0.2917999993205068, 0.028224000525951372)
    }

    fun mixedHeight2() {
        val player = createPlayer(createConnection(3))
        player.connection.world[Vec3i(10, 1, 4)] = StoneTest0.state
        player.connection.world[Vec3i(10, 2, 4)] = lava!!.states.withProperties(BlockProperties.FLUID_LEVEL to 7)
        player.connection.world[Vec3i(10, 3, 4)] = water!!.states.withProperties(BlockProperties.FLUID_LEVEL to 7)
        player.forceTeleport(Vec3d(10.0, 2.0, 3.8))
        player.runTicks(2)
        player.input = PlayerMovementInput(jump = true, forward = true)
        player.runTicks(3)

        player.assertPosition(10.0, 3.1227999789357184, 3.902704000544429)
        player.assertVelocity(0.0, 0.2604400026965139, 0.038259200945568075)
    }

    fun mixedHeight3() {
        val player = createPlayer(createConnection(3))
        player.connection.world[Vec3i(10, 1, 4)] = StoneTest0.state
        player.connection.world[Vec3i(10, 2, 4)] = water!!.states.withProperties(BlockProperties.FLUID_LEVEL to 7)
        player.connection.world[Vec3i(10, 3, 4)] = lava!!.states.withProperties(BlockProperties.FLUID_LEVEL to 7)
        player.forceTeleport(Vec3d(10.0, 2.0, 3.8))
        player.runTicks(2)
        player.input = PlayerMovementInput(jump = true, forward = true)
        player.runTicks(2)

        player.assertPosition(10.0, 2.0979999979138375, 3.854880000075102)
        player.assertVelocity(0.0, 0.04539999979734419, 0.028224000525951372)
    }

    fun mixedHeight4() {
        val player = createPlayer(createConnection(3))
        player.connection.world[Vec3i(10, 1, 4)] = StoneTest0.state
        player.connection.world[Vec3i(10, 2, 4)] = water!!.states.withProperties(BlockProperties.FLUID_LEVEL to 7)
        player.connection.world[Vec3i(10, 3, 4)] = lava!!.states.withProperties(BlockProperties.FLUID_LEVEL to 7)
        player.forceTeleport(Vec3d(10.0, 2.0, 3.8))
        player.runTicks(2)
        player.input = PlayerMovementInput(jump = true, forward = true)
        player.runTicks(3)

        player.assertPosition(10.0, 2.183399996817112, 3.902704000544429)
        player.assertVelocity(0.0, 0.06332000014066692, 0.038259200945568075)
    }
}
