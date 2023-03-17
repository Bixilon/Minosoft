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

import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class ScaffoldingIT : ClimbingIT() {

    @Test(priority = -1)
    fun getScaffolding() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.SCAFFOLDING]?.defaultState ?: throw SkipException("Can not find scaffolding!")
    }

    fun scaffoldingFallingInto1() {
        val player = super.fallingInto1()
        player.assertPosition(5.0, 11.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun scaffoldingFallingInto2() {
        val player = super.fallingInto2()
        player.assertPosition(5.0, 9.586654892508815, 5.0)
        player.assertVelocity(0.0, -0.22540001022815717, 0.0)
        player.assertGround(false)
    }

    fun scaffoldingFallingInto3() {
        val player = super.fallingInto3()
        player.assertPosition(5.0, 11.796735600668693, 5.0)
        player.assertVelocity(0.0, -0.30153472366278034, 0.0)
        player.assertGround(false)
    }

    fun scaffoldingWalkingForwards1() {
        val player = super.walkingForwards1()
        player.assertPosition(5.0, 4.653729617588595, 7.905375174498606)
        player.assertVelocity(0.0, -0.7170746714356033, 0.1576552866404768)
        player.assertGround(false)
    }

    fun scaffoldingWalkingForwards2() {
        val player = super.walkingForwards2()
        player.assertPosition(5.0, -8.00159582139278, 9.607800764005013)
        player.assertVelocity(0.0, -1.2495683414761176, 0.1808370277231683)
        player.assertGround(false)
    }

    fun scaffoldingWalkingSideways1() {
        val player = super.walkingSideways1()
        player.assertPosition(7.905375174498606, 4.653729617588595, 5.0)
        player.assertVelocity(0.1576552866404768, -0.7170746714356033, 0.0)
        player.assertGround(false)
    }

    fun scaffoldingWalkingCombined1() {
        val player = super.walkingCombined1()
        player.assertPosition(7.390313290491049, 5.305438451751212, 7.390313290491049)
        player.assertVelocity(0.11278079011373845, -0.6517088341626173, 0.11278079011373845)
        player.assertGround(false)
    }

    fun scaffoldingSneaking1() {
        val player = super.sneaking1()
        player.assertPosition(5.0, 5.22159989118576, 5.0)
        player.assertVelocity(0.0, -0.22540001022815717, 0.0)
        player.assertGround(false)
    }

    fun scaffoldingSneaking2() {
        val player = super.sneaking2()
        player.assertPosition(5.0, 4.321599855422973, 5.0)
        player.assertVelocity(0.0, -0.22540001022815717, 0.0)
        player.assertGround(false)
    }

    fun scaffoldingJumping1() {
        val player = super.jumping1()
        player.assertPosition(5.0, 10.234400043487533, 5.0)
        player.assertVelocity(0.0, 0.11760000228881837, 0.0)
        player.assertGround(false)
    }

    fun scaffoldingJumping2() {
        val player = super.jumping2()
        player.assertPosition(5.0, 11.052159104547403, 5.0)
        player.assertVelocity(0.0, -0.11984318109609361, 0.0)
        player.assertGround(false)
    }

    fun scaffoldingJumpingForwards1() {
        val player = super.jumpingForwards1()
        player.assertPosition(5.0, 7.459001725883314, 7.48749891308278)
        player.assertVelocity(0.0, -0.5536600782531381, 0.1681251619273165)
        player.assertGround(false)
    }

    fun scaffoldingJumpingForwards2() {
        val player = super.jumpingForwards2()
        player.assertPosition(5.0, 4.030764463918555, 8.450845430024417)
        player.assertVelocity(0.0, -0.8770954060316738, 0.17942400038421488)
        player.assertGround(false)
    }

    fun scaffoldingClimbingDown1() {
        val player = super.climbingDown1()
        player.assertPosition(5.0, 8.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun scaffoldingClimbingDown2() {
        val player = super.climbingDown2()
        player.assertPosition(5.0, 8.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun scaffoldingClimbingUpCollision() {
        val player = super.climbingUpCollision()
        player.assertPosition(5.0, 8.200000047683716, 5.0)
        player.assertVelocity(0.0, 0.11760000228881837, 0.0)
        player.assertGround(false)
    }

    fun scaffoldingClimbingUpTrapdoor() {
        val player = super.climbingUpTrapdoor()
        player.assertPosition(5.0,11.249187078744683, 5.0)
        player.assertVelocity(0.0, 0.0030162615090425808, 0.0)
        player.assertGround(false)
    }
}
