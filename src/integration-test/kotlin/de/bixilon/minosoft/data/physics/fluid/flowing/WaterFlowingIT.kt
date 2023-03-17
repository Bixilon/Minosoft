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

package de.bixilon.minosoft.data.physics.fluid.flowing

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class WaterFlowingIT : FlowingFluidIT() {

    @Test(priority = -1)
    fun getWater() {
        this.block = IT.REGISTRIES.block[WaterFluid].unsafeCast()
    }

    fun waterStarMiddle() {
        val player = super.starMiddle()
        player.assertPosition(4.5, 4.0, 4.5)
        player.assertVelocity(0.0, -0.005, 0.0)
        player.assertGround(true)
    }

    fun waterStarOffset() {
        val player = super.starOffset()
        player.assertPosition(3.108210789369952, 4.0, 3.108210789369952)
        player.assertVelocity(-0.036614189714959076, -0.005, -0.036614189714959076)
        player.assertGround(true)
    }

    fun waterStarOffset2() {
        val player = super.starOffset2()
        player.assertPosition(2.6756500665059177, 4.0, 2.6756500665059177)
        player.assertVelocity(-0.03919750472818282, -0.005, -0.03919750472818282)
        player.assertGround(true)
    }

    fun waterStarOffset3() {
        val player = super.starOffset3()

        player.assertPosition(3.3159617347595995, 4.0, 9.684038265240401)
        player.assertVelocity(-0.008689739732951745, -0.005, 0.008689739732951745)
        player.assertGround(true)
    }

    fun waterStartJumping() {
        val player = super.startJumping()

        player.assertPosition(3.0743292409910747, 4.748792123451086, 3.0743292409910747)
        player.assertVelocity(-0.013639913468259983, -0.13109995848408168, -0.013639913468259983)
        player.assertGround(false)
    }

    fun waterLowJumping() {
        val player = super.lowJumping()
        player.assertPosition(2.6957498366308634, 4.404375995798588, 2.6957498366308634)
        player.assertVelocity(-0.02940553933519609, 0.08912480119047161, -0.02940553933519609)
        player.assertGround(false)
    }

    fun waterLowJumping2() {
        val player = super.lowJumping2()

        player.assertPosition(3.22602542251511, 5.240647579523717, 10.27397457748489)
        player.assertVelocity(-0.005400741256642114, 9.870522127327233E-4, 0.005400741256642114)
        player.assertGround(false)
    }
}
