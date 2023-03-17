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
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.test.IT
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class WaterStillIT : StillFluidIT() {

    @Test(priority = -1)
    fun getWater() {
        this.block = IT.REGISTRIES.block[WaterFluid].unsafeCast()
    }

    fun waterLanding1() {
        val player = super.landing1()
        player.assertPosition(4.0, 15.507679483126383, 7.0)
        player.assertVelocity(0.0, -0.19805514979906275, 0.0)
        player.assertGround(false)
    }

    fun waterLanding2() {
        val player = super.landing2()
        player.assertPosition(4.0, 10.208774596169043, 7.0)
        player.assertVelocity(0.0, -0.06996482977729239, 0.0)
        player.assertGround(false)
    }

    fun waterWalking1() {
        val player = super.walking1()
        player.assertPosition(4.0, 16.0, 7.679885855605685)
        player.assertVelocity(0.0, -0.005, 0.0722001417819209)
        player.assertGround()
    }

    fun waterWalking2() {
        val player = super.walking2()
        player.assertPosition(4.0, 16.0, 2.627106815886404)
        player.assertVelocity(0.0, -0.005, -0.0783987179526886)
        player.assertGround()
    }

    fun waterWalking3() {
        val player = super.walking3()
        player.assertPosition(4.0, 16.0, 2.6879910427488936)
        player.assertVelocity(0.0, -0.005, -0.07839825723495852)
        player.assertGround()
    }

    fun waterSprinting1() {
        val player = super.sprinting1()
        player.assertPosition(4.875761690130636, 16.0, 7.875761690130636)
        player.assertVelocity(0.055609437174596786, -0.005, 0.055609437174596786)
        player.assertGround()
    }

    fun waterJumping1() {
        val player = super.jumping1()
        player.assertPosition(4.0, 16.458418725139527, 7.0)
        player.assertVelocity(0.0, -0.09893752060854599, 0.0)
        player.assertGround(false)
    }

    fun waterSinking1() {
        val player = super.sinking1()
        player.assertPosition(4.0, 19.74560194453099, 7.0)
        player.assertVelocity(0.0, -0.024120391938858576, 0.0)
        player.assertGround(false)
    }

    fun waterSinking2() {
        val player = super.sinking2()
        player.assertPosition(4.0, 18.424758187884287, 7.0)
        player.assertVelocity(0.0, -0.024951644434273192, 0.0)
        player.assertGround(false)
    }

    fun waterSwimUpwards1() {
        val player = super.swimUpwards1()
        player.assertPosition(4.0, 21.186305733855328, 7.0)
        player.assertVelocity(0.0, 0.05358012584344959, 0.0)
        player.assertGround(false)
    }

    fun waterSwimUpwards2() {
        val player = super.swimUpwards2()
        player.assertPosition(4.0, 21.186305733855328, 9.35275834287884)
        player.assertVelocity(0.0, 0.05358012584344959, 0.08900750833397757)
        player.assertGround(false)
    }

    fun waterSwimUpwards3() {
        val player = super.swimUpwards3()
        player.assertPosition(4.0, 21.11827017581037, 7.0)
        player.assertVelocity(0.0, -0.052768412702618644, 0.0)
        player.assertGround(false)
    }

    fun waterSwimUpwards4() {
        val player = super.swimUpwards4()
        player.assertPosition(4.0, 20.147477577018837, 7.0)
        player.assertVelocity(0.0, 0.1205044893345342, 0.0)
        player.assertGround(false)
    }

    fun waterKnockDownwards() {
        val player = super.knockDownwards()
        player.assertPosition(4.0, 18.77640000290871, 7.0)
        player.assertVelocity(0.0, -0.09028000056505202, 0.0)
        player.assertGround(false)
    }

    fun waterKnockDownwards2() {
        val player = super.knockDownwards2()
        player.assertPosition(4.0, 17.77584859479186, 7.0)
        player.assertVelocity(0.0, -0.1601697255047665, 0.0)
        player.assertGround(false)
    }

    fun waterKnockDownwards3() {
        val player = super.knockDownwards3()
        player.assertPosition(4.0, 16.0, 7.0)
        player.assertVelocity(0.0, -0.005, 0.0)
        player.assertGround()
    }
    // TODO: water[FALLING=true]
}
