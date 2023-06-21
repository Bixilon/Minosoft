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
class TwistedVinesIT : ClimbingIT() {

    @Test(priority = -1)
    fun getTwistingVines() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.TWISTING_VINES]?.states?.default ?: throw SkipException("Can not find twisting vines!")
    }

    fun twistedVinesFallingInto1() {
        val player = super.fallingInto1()
        player.assertPosition(5.0, 9.586654892508815, 5.0)
        player.assertVelocity(0.0, -0.22540001022815717, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesFallingInto2() {
        val player = super.fallingInto2()
        player.assertPosition(5.0, 10.936654946152995, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesFallingInto3() {
        val player = super.fallingInto3()
        player.assertPosition(5.0, 11.038886680903556, 5.0)
        player.assertVelocity(0.0, 0.03684800296020513, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesWalkingForwards1() {
        val player = super.walkingForwards1()
        player.assertPosition(5.0, 2.6301464692052106, 7.48749891308278)
        player.assertVelocity(0.0, -0.7767710253581391, 0.1681251619273165)
        player.assertGround(false)
    }

    fun twistedVinesWalkingForwards2() {
        val player = super.walkingForwards2()
        player.assertPosition(5.0, -10.521411628722896, 9.25647454960242)
        player.assertVelocity(0.0, -1.2993400516846056, 0.1853174005241614)
        player.assertGround(false)
    }

    fun twistedVinesWalkingSideways1() {
        val player = super.walkingSideways1()
        player.assertPosition(7.48749891308278, 2.6301464692052106, 5.0)
        player.assertVelocity(0.1681251619273165, -0.7767710253581391, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesWalkingCombined1() {
        val player = super.walkingCombined1()
        player.assertPosition(6.794823791225974, 3.6899367391283855, 6.794823791225974)
        player.assertVelocity(0.12130861201338133, -0.647166802007322, 0.12130861201338133)
        player.assertGround(false)
    }

    fun twistedVinesSneaking1() {
        val player = super.sneaking1()
        player.assertPosition(5.0, 8.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesSneaking2() {
        val player = super.sneaking2()
        player.assertPosition(5.0, 8.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesJumping1() {
        val player = super.jumping1()
        player.assertPosition(5.0, 10.234400043487533, 5.0)
        player.assertVelocity(0.0, 0.11760000228881837, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesJumping2() {
        val player = super.jumping2()
        player.assertPosition(5.0, 11.052159104547403, 5.0)
        player.assertVelocity(0.0, -0.11984318109609361, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesJumpingForwards1() {
        val player = super.jumpingForwards1()
        player.assertPosition(5.0, 7.459001725883314, 7.48749891308278)
        player.assertVelocity(0.0, -0.5536600782531381, 0.1681251619273165)
        player.assertGround(false)
    }

    fun twistedVinesJumpingForwards2() {
        val player = super.jumpingForwards2()
        player.assertPosition(5.0, 4.030764463918555, 8.450845430024417)
        player.assertVelocity(0.0, -0.8770954060316738, 0.17942400038421488)
        player.assertGround(false)
    }

    fun twistedVinesClimbingDown1() {
        val player = super.climbingDown1()
        player.assertPosition(5.0, 5.22159989118576, 5.0)
        player.assertVelocity(0.0, -0.22540001022815717, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesClimbingDown2() {
        val player = super.climbingDown2()
        player.assertPosition(5.0, 4.171599849462509, 5.0)
        player.assertVelocity(0.0, -0.22540001022815717, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesClimbingUpCollision() {
        val player = super.climbingUpCollision()
        player.assertPosition(5.0, 8.200000047683716, 5.0)
        player.assertVelocity(0.0, 0.11760000228881837, 0.0)
        player.assertGround(false)
    }

    fun twistedVinesClimbingUpTrapdoor() {
        val player = super.climbingUpTrapdoor()
        player.assertPosition(5.0,10.101431773660458, 5.0)
        player.assertVelocity(0.0, 0.03684800296020513, 0.0)
        player.assertGround(false)
    }
}
