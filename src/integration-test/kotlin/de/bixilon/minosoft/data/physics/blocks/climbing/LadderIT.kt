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
class LadderIT : ClimbingIT() {

    @Test(priority = -1)
    fun getLadder() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.LADDER]?.states?.default ?: throw SkipException("Can not find ladder!")
    }

    fun ladderFallingInto1() {
        val player = super.fallingInto1()
        player.assertPosition(5.0, 9.586654892508815, 5.0)
        player.assertVelocity(0.0, -0.22540001022815717, 0.0)
        player.assertGround(false)
    }

    fun ladderFallingInto2() {
        val player = super.fallingInto2()
        player.assertPosition(5.0, 10.936654946152995, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun ladderFallingInto3() {
        val player = super.fallingInto3()
        player.assertPosition(5.0, 11.038886680903556, 5.0)
        player.assertVelocity(0.0, 0.03684800296020513, 0.0)
        player.assertGround(false)
    }

    fun ladderWalkingForwards1() {
        val player = super.walkingForwards1()
        player.assertPosition(5.0, 8.432799990177152, 5.512499988079071)
        player.assertVelocity(0.0, 0.11760000228881837, 0.0)
        player.assertGround(false)
    }

    fun ladderWalkingForwards2() {
        val player = super.walkingForwards2()
        player.assertPosition(5.0, 5.49120001077652, 5.512499988079071)
        player.assertVelocity(0.0, 0.11760000228881837, 0.0)
        player.assertGround(false)
    }

    fun ladderWalkingSideways1() {
        val player = super.walkingSideways1()
        player.assertPosition(7.48749891308278, 2.6301464692052106, 5.0)
        player.assertVelocity(0.1681251619273165, -0.7767710253581391, 0.0)
        player.assertGround(false)
    }

    fun ladderWalkingCombined1() {
        val player = super.walkingCombined1()
        player.assertPosition(6.794823791225974, 6.257217283679258, 5.641755869625952)
        player.assertVelocity(0.12130861201338133, -0.41485637049930457, 0.04493551528116944)
        player.assertGround(false)
    }

    fun ladderSneaking1() {
        val player = super.sneaking1()
        player.assertPosition(5.0, 8.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun ladderSneaking2() {
        val player = super.sneaking2()
        player.assertPosition(5.0, 8.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun ladderJumping1() {
        val player = super.jumping1()
        player.assertPosition(5.0, 10.234400043487533, 5.0)
        player.assertVelocity(0.0, 0.11760000228881837, 0.0)
        player.assertGround(false)
    }

    fun ladderJumping2() {
        val player = super.jumping2()
        player.assertPosition(5.0, 11.052159104547403, 5.0)
        player.assertVelocity(0.0, -0.11984318109609361, 0.0)
        player.assertGround(false)
    }

    fun ladderJumpingForwards1() {
        val player = super.jumpingForwards1()
        player.assertPosition(5.0, 10.234400043487533, 5.512499988079071)
        player.assertVelocity(0.0, 0.11760000228881837, 0.0)
        player.assertGround(false)
    }

    fun ladderJumpingForwards2() {
        val player = super.jumpingForwards2()
        player.assertPosition(5.0, 10.82240005493162, 5.512499988079071)
        player.assertVelocity(0.0, 0.11760000228881837, 0.0)
        player.assertGround(false)
    }

    fun ladderClimbingDown1() {
        val player = super.climbingDown1()
        player.assertPosition(5.0, 5.22159989118576, 5.0)
        player.assertVelocity(0.0, -0.22540001022815717, 0.0)
        player.assertGround(false)
    }

    fun ladderClimbingDown2() {
        val player = super.climbingDown2()
        player.assertPosition(5.0, 4.171599849462509, 5.0)
        player.assertVelocity(0.0, -0.22540001022815717, 0.0)
        player.assertGround(false)
    }

    fun ladderClimbingUpCollision() {
        val player = super.climbingUpCollision()
        player.assertPosition(5.0, 8.200000047683716, 5.0)
        player.assertVelocity(0.0, 0.11760000228881837, 0.0)
        player.assertGround(false)
    }

    fun ladderClimbingUpTrapdoor() {
        val player = super.climbingUpTrapdoor()
        player.assertPosition(5.0, 11.057600059509255, 5.0)
        player.assertVelocity(0.0, 0.03684800296020513, 0.0)
        player.assertGround(false)
    }
}
