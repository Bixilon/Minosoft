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

package de.bixilon.minosoft.data.physics.blocks.slow

import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class SweetBerryBushIT : SlowMovementIT() {

    @Test(priority = -1)
    fun getSweetBerryBush() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.SWEET_BERRY_BUSH]?.states?.default ?: throw SkipException("Can not find sweet berry bush!")
    }

    fun sweetBerryBushLanding() {
        val player = landing()

        player.assertPosition(5.0, 10.342869873177566, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun sweetBerryBushFalling() {
        val player = falling()

        player.assertPosition(5.0, 10.39357598707581, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun sweetBerryBushForwardsMovement() {
        val player = forwardsMovement()

        player.assertPosition(5.0, 10.0, 5.676748831613397)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun sweetBerryBushFallingForwardsMovement() {
        val player = fallingForwardsMovement()

        player.assertPosition(5.0, 10.39357598707581, 5.209729411343082)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun sweetBerryBushSidewaysMovement1() {
        val player = super.sidewaysMovement1()

        player.assertPosition(5.209729411343082, 10.39357598707581, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun sweetBerryBushSidewaysMovement2() {
        val player = super.sidewaysMovement2()

        player.assertPosition(4.790270588656918, 10.39357598707581, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun sweetBerryBushCombinedMovement() {
        val player = super.combinedMovement()

        player.assertPosition(4.848672361134072, 10.39357598707581, 5.151327638865928)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }
    fun sweetBerryBushStanding() {
        val player = super.standing()

        player.assertPosition(5.0, 10.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun sweetBerryBushStandingJump1() {
        val player = super.standingJump1()

        player.assertPosition(5.0, 10.314999990165234, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun sweetBerryBushStandingJump2() {
        val player = super.standingJump2()

        player.assertPosition(5.0, 10.197399987876416, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }
}
