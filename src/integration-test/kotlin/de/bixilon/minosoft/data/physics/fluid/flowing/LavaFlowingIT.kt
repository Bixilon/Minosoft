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
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class LavaFlowingIT : FlowingFluidIT() {

    @Test(priority = -1)
    fun getLava() {
        this.block = IT.REGISTRIES.block[LavaFluid].unsafeCast()
    }

    fun lavaStarMiddle() {
        val player = super.starMiddle()
        player.assertPosition(4.5, 4.0, 4.5)
        player.assertVelocity(0.0, -0.02, 0.0)
        player.assertGround(true)
    }

    fun lavaStarOffset() {
        val player = super.starOffset()
        player.assertPosition(3.4357161790474278, 4.0, 3.4357161790474278)
        player.assertVelocity(-0.0029777263226456454, -0.02, -0.0029777263226456454)
        player.assertGround(true)
    }

    fun lavaStarOffset2() {
        val player = super.starOffset2()
        player.assertPosition(3.3847730217170664, 4.0, 3.3847730217170664)
        player.assertVelocity(-0.003079842910378326, -0.02, -0.003079842910378326)
        player.assertGround(true)
    }

    fun lavaStarOffset3() {
        val player = super.starOffset3()
        player.assertPosition(3.402163173983297, 4.0, 9.597836826016703)
        player.assertVelocity(-0.0030882698959512735, -0.025, 0.0030882698959512735)
        player.assertGround(true)
    }

    fun lavaStartJumping() {
        val player = super.startJumping()

        player.assertPosition(3.413324424748012, 4.371999989703298, 3.413324424748012)
        player.assertVelocity(-0.0029777065417247437, 0.01259999965429304, -0.0029777065417247437)
        player.assertGround(false)
    }

    fun lavaLowJumping() {
        val player = super.lowJumping()
        player.assertPosition(3.3847730217170664, 4.164999994412065, 3.3847730217170664)
        player.assertVelocity(-0.003079842910378326, -4.470348362317633E-10, -0.003079842910378326)
        player.assertGround(false)
    }

    fun lavaLowJumping2() {
        val player = super.lowJumping2()
        player.assertPosition(3.373935307919407, 5.163015737295476, 10.126064692080591)
        player.assertVelocity(0.0, -0.017460312423412366, 0.0)
        player.assertGround(false)
    }
}
