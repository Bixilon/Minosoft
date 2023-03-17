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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class LavaStillIT : StillFluidIT() {

    @Test(priority = -1)
    fun getLava() {
        this.block = IT.REGISTRIES.block[LavaFluid].unsafeCast()
    }

    fun lavaLanding1() {
        val player = super.landing1()
        player.assertPosition(4.0, 15.687148337252625, 7.0)
        player.assertVelocity(0.0, -0.0828747903651656, 0.0)
        player.assertGround(false)
    }

    fun lavaLanding2() {
        val player = super.landing2()
        player.assertPosition(4.0, 12.831221018844722, 7.0)
        player.assertVelocity(0.0, -0.040062706397432715, 0.0)
        player.assertGround(false)
    }

    fun lavaWalking1() {
        val player = super.walking1()
        player.assertPosition(4.0, 16.0, 7.443119445050214)
        player.assertVelocity(0.0, -0.02, 0.01982841208589204)
        player.assertGround()
    }

    fun lavaWalking2() {
        val player = super.walking2()
        player.assertPosition(4.0, 16.0, 5.066823734968211)
        player.assertVelocity(0.0, -0.02, -0.01959999994337641)
        player.assertGround()
    }

    fun lavaWalking3() {
        val player = super.walking3()
        player.assertPosition(4.0, 16.0, 5.157600005322696)
        player.assertVelocity(0.0, -0.02, -0.01959999994337551)
        player.assertGround()
    }

    fun lavaSprinting1() {
        val player = super.sprinting1()
        player.assertPosition(4.488558729386883, 16.0, 7.488558729386883)
        player.assertVelocity(0.014149085881453943, -0.02, 0.014149085881453943)
        player.assertGround()
    }
    fun lavaJumping1() {
        val player = super.jumping1()
        player.assertPosition(4.0, 16.438418725139528, 7.0)
        player.assertVelocity(0.0, -0.13493752084696456, 0.0)
        player.assertGround(false)
    }

    fun lavaSinking1() {
        val player = super.sinking1()
        player.assertPosition(4.0, 19.479997558593748, 7.0)
        player.assertVelocity(0.0, -0.039998779296874995, 0.0)
        player.assertGround(false)
    }

    fun lavaSinking2() {
        val player = super.sinking2()
        player.assertPosition(4.0, 17.959999999701974, 7.0)
        player.assertVelocity(0.0, -0.039999999850988385, 0.0)
        player.assertGround(false)
    }

    fun lavaSwimUpwards1() {
        val player = super.swimUpwards1()
        player.assertPosition(4.0, 18.11999997496605, 7.0)
        player.assertVelocity(0.0, -4.470348362317633E-10, 0.0)
        player.assertGround(false)
    }

    fun lavaSwimUpwards2() {
        val player = super.swimUpwards2()
        player.assertPosition(4.0, 18.11999997496605, 8.058399997088312)
        player.assertVelocity(0.0, -4.470348362317633E-10, 0.019599999870359888)
        player.assertGround(false)
    }

    fun lavaSwimUpwards3() {
        val player = super.swimUpwards3()
        player.assertPosition(4.0, 20.72925210004758, 7.0)
        player.assertVelocity(0.0, -0.007904422288862893, 0.0)
        player.assertGround(false)
    }

    fun lavaSwimUpwards4() {
        val player = super.swimUpwards4()
        player.assertPosition(4.0, 19.399999991059303, 7.0)
        player.assertVelocity(0.0, -4.470348362317633E-10, 0.0)
        player.assertGround(false)
    }

    fun lavaKnockDownwards() {
        val player = super.knockDownwards()
        player.assertPosition(4.0, 18.95, 7.0)
        player.assertVelocity(0.0, -0.035, 0.0)
        player.assertGround(false)
    }

    fun lavaKnockDownwards2() {
        val player = super.knockDownwards2()
        player.assertPosition(4.0, 18.71984375, 7.0)
        player.assertVelocity(0.0, -0.039921874999999996, 0.0)
        player.assertGround(false)
    }

    fun lavaKnockDownwards3() {
        val player = super.knockDownwards3()
        player.assertPosition(4.0, 18.31999984741211, 7.0)
        player.assertVelocity(0.0, -0.039999923706054694, 0.0)
        player.assertGround(false)
    }
}
