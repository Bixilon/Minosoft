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
class SlimeWalkIT : WalkIT() {

    @Test(priority = -1)
    fun getSlime() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.SLIME_BLOCK]?.defaultState ?: throw SkipException("Can not find slime!")
    }

    fun slimeLanding1() {
        val player = super.landing()

        player.assertPosition(6.0, 2.959247410855058, 6.0)
        player.assertVelocity(0.0, 0.27154827320718833, 0.0)
    }

    fun slimeWalking1() {
        val player = super.walking1()

        player.assertPosition(6.0, 1.0, 6.057036000349253)
        player.assertVelocity(0.0, -0.001567998535156222, 0.014160871368841135)
    }

    fun slimeWalking2() {
        val player = super.walking2()

        player.assertPosition(6.0, 1.0, 6.572589815054638)
        player.assertVelocity(0.0, -0.001567998535156222, 0.02587589640486239)
    }

    fun slimeWalking3() {
        val player = super.walking3()

        player.assertPosition(6.0, 1.0, 9.289881243431806)
        player.assertVelocity(0.0, -0.001567998535156222, 0.025943656768869314)
    }

    fun slimeWalking4() {
        val player = super.walking4()

        player.assertPosition(-6.0, 1.0, -5.427410184945362)
        player.assertVelocity(0.0, -0.001567998535156222, 0.02587589640486239)
    }

    fun slimeWalking5() {
        val player = super.walking5()

        player.assertPosition(-6.0, 1.0, -2.7101187565682006)
        player.assertVelocity(0.0, -0.001567998535156222, 0.025943656768869314)
    }

    fun slimeWalking6() {
        val player = super.walking6()

        player.assertPosition(-6.0, 1.0, -6.572589815054638)
        player.assertVelocity(-9.079615870990738E-19, -0.001567998535156222, -0.02587589640486239)
    }

    fun slimeWalking7() {
        val player = super.walking7()

        player.assertPosition(-6.0, 1.0, -9.289881243431806)
        player.assertVelocity(-9.079615870990738E-19, -0.001567998535156222, -0.025943656768869314)
    }

    fun slimeWalking8() {
        val player = super.walking8()

        player.assertPosition(-6.057085541768311, 1.0, -5.954937565109846)
        player.assertVelocity(0.025902571049725594, -0.001567998535156222, 0.0)
    }

    fun slimeWalking9() {
        val player = super.walking9()

        player.assertPosition(-10.816527267105863, 1.0, -3.371124340372769)
        player.assertVelocity(-0.04112470532380117, -0.0784000015258789, -0.026569124504444985)
    }

    fun slimeSprintJump1() {
        val player = super.sprintJump1()

        player.assertPosition(-6.0, 1.0, -5.937083998572499)
        player.assertVelocity(0.0, -0.001567998535156222, 0.01638509238644333)
    }

    fun slimeSprintJump2() {
        val player = super.sprintJump2()

        player.assertPosition(-6.0, 2.0013359791121474, -5.217192074519572)
        player.assertVelocity(0.0, 0.16477328182606651, 0.20713771125079936)
    }

    fun slimeSlabWalk() {
        val player = super.slabWalk()

        player.assertPosition(-6.0, 1.5, -5.422694145343175)
        player.assertVelocity(0.0, -0.0784000015258789, 0.016584649856195698)
        player.assertGround()
    }
}
