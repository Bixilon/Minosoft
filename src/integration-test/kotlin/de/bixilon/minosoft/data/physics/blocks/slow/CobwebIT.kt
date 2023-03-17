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
class CobwebIT : SlowMovementIT() {

    @Test(priority = -1)
    fun getCobweb() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.COBWEB]?.defaultState ?: throw SkipException("Can not find cobweb!")
    }

    fun cobwebLanding() {
        val player = landing()

        player.assertPosition(5.0, 10.820758403761651, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun cobwebFalling() {
        val player = falling()

        player.assertPosition(5.0, 10.886398397189684, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun cobwebForwardsMovement() {
        val player = forwardsMovement()

        player.assertPosition(5.0, 10.0, 5.22495900678017)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun cobwebForwardsFallingMovement() {
        val player = fallingForwardsMovement()

        player.assertPosition(5.0, 10.886398397189684, 5.104752690573788)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun cobwebSidewaysMovement1() {
        val player = super.sidewaysMovement1()

        player.assertPosition(5.104752690573788, 10.886398397189684, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun cobwebSidewaysMovement2() {
        val player = super.sidewaysMovement2()

        player.assertPosition(4.895247309426212, 10.886398397189684, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun cobwebCombinedMovement() {
        val player = super.combinedMovement()

        player.assertPosition(4.92441700366262, 10.886398397189684, 5.07558299633738)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun cobwebStanding() {
        val player = super.standing()

        player.assertPosition(5.0, 10.0, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }

    fun cobwebStandingJump1() {
        val player = super.standingJump1()

        player.assertPosition(5.0, 10.020999999657274, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }

    fun cobwebStandingJump2() {
        val player = super.standingJump2()

        player.assertPosition(5.0, 10.01315999938786, 5.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround(false)
    }
}
