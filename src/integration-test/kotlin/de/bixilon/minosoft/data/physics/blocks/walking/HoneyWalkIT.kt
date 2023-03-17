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

package de.bixilon.minosoft.data.physics.blocks.walking

import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertGround
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertVelocity
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.test.IT
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["physics"], dependsOnGroups = ["block"])
class HoneyWalkIT : WalkIT() {

    @Test(priority = -1)
    fun getHoney() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.HONEY_BLOCK]?.defaultState ?: throw SkipException("Can not find honey!")
    }

    fun honeyLanding2() {
        val player = super.landing()

        player.assertPosition(6.0, 0.9375, 6.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun honeyWalking1() {
        val player = super.walking1()

        player.assertPosition(6.0, 0.9375, 6.046334400178063)
        player.assertVelocity(0.0, -0.0784000015258789, 0.009731322110889201)
    }

    fun honeyWalking2() {
        val player = super.walking2()

        player.assertPosition(6.0, 0.9375, 7.026820115266771)
        player.assertVelocity(0.0, -0.0784000015258789, 0.027383742202465887)
    }

    fun honeyWalking3() {
        val player = super.walking3()

        player.assertPosition(6.0, 0.9375, 12.04217347616948)
        player.assertVelocity(0.0, -0.0784000015258789, 0.027383833577600523)
    }

    fun honeyWalking4() {
        val player = super.walking4()

        player.assertPosition(-6.0, 0.9375, -4.973179884733229)
        player.assertVelocity(0.0, -0.0784000015258789, 0.027383742202465887)
    }

    fun honeyWalking5() {
        val player = super.walking5()

        player.assertPosition(-6.0, 0.9375, 0.04217347616948944)
        player.assertVelocity(0.0, -0.0784000015258789, 0.027383833577600523)
    }

    fun honeyWalking6() {
        val player = super.walking6()

        player.assertPosition(-6.0, 0.9375, -7.026820115266771)
        player.assertVelocity(-2.6211365851074295E-18, -0.0784000015258789, -0.027383742202465887)
    }

    fun honeyWalking7() {
        val player = super.walking7()

        player.assertPosition(-6.0, 0.9375, -12.04217347616948)
        player.assertVelocity(-2.6211365851074295E-18, -0.0784000015258789, -0.027383833577600523)
    }

    fun honeyWalking8() {
        val player = super.walking8()

        player.assertPosition(-6.033364455376936, 0.9375, -5.808017246378655)
        player.assertVelocity(0.027383826816441414, -0.0784000015258789, 0.0)
    }

    fun honeyWalking9() {
        val player = super.walking9()

        player.assertPosition(-14.991874547407981, 0.9375, -1.3499466022934652)
        player.assertVelocity(-0.02279835578593394, -0.0784000015258789, -0.015165410888227721)
    }

    fun honeySprintJump1() {
        val player = super.sprintJump1()

        player.assertPosition(-6.0, 0.9375, -5.947785598743689)
        player.assertVelocity(0.0, -0.0784000015258789, 0.011871642596948414)
    }

    fun honeySprintJump2() {
        val player = super.sprintJump2()

        player.assertPosition(-6.0, 1.3213519865348335, -5.4472109908493085)
        player.assertVelocity(0.0, -0.03287704354344872, 0.02246826828413226)
    }

    fun honeySlabWalk() {
        val player = super.slabWalk()

        player.assertPosition(-6.0, 1.5, -5.566723500360934)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
        player.assertGround()
    }
}
