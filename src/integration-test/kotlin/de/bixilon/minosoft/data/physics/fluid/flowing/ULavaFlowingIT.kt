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
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class ULavaFlowingIT : FlowingFluidIT() {

    @Test(priority = -1)
    fun getLava() {
        this.block = IT.REGISTRIES.block[LavaFluid].unsafeCast()
    }

    override fun create(): LocalPlayerEntity {
        val player = super.create()
        player.connection.world.dimension::ultraWarm.forceSet(true)
        return player
    }

    fun ulavaStarMiddle() {
        val player = super.starMiddle()
        player.assertPosition(4.5, 4.0, 4.5)
        player.assertVelocity(0.0, -0.02, 0.0)
        player.assertGround(true)
    }

    fun ulavaStarOffset() {
        val player = super.starOffset()
        player.assertPosition(3.3946378095668663, 4.0, 3.3946378095668663)
        player.assertVelocity(-0.0049481074501365775, -0.02, -0.0049481074501365775)
        player.assertGround(true)
    }

    fun ulavaStarOffset2() {
        val player = super.starOffset2()
        player.assertPosition(3.3055456287673795, 4.0, 3.3055456287673795)
        player.assertVelocity(-0.004949744265145347, -0.02, -0.004949744265145347)
        player.assertGround(true)
    }

    fun ulavaStarOffset3() {
        val player = super.starOffset3()
        player.assertPosition(3.401031534021715, 4.0, 9.598968465978288)
        player.assertVelocity(-0.00262545337643569, -0.025, 0.00262545337643569)
        player.assertGround(true)
    }

    fun ulavaStartJumping() {
        val player = super.startJumping()

        player.assertPosition(3.3629047556100695, 4.371999989703298, 3.3629047556100695)
        player.assertVelocity(-0.0025514917616342525, 0.01259999965429304, -0.0025514917616342525)
        player.assertGround(false)
    }

    fun ulavaLowJumping() {
        val player = super.lowJumping()
        player.assertPosition(3.3055456287673795, 4.164999994412065, 3.3055456287673795)
        player.assertVelocity(-0.004949744265145347, -4.470348362317633E-10, -0.004949744265145347)
        player.assertGround(false)
    }

    fun ulavaLowJumping2() {
        val player = super.lowJumping2()
        player.assertPosition(3.3648136809604705, 5.163015737295476, 10.135186319039533)
        player.assertVelocity(0.0, -0.017460312423412366, 0.0)
        player.assertGround(false)
    }
}
