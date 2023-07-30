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
class StoneWalkIT : WalkIT() {

    @Test(priority = -1)
    fun getStone() {
        this.block = IT.REGISTRIES.block[MinecraftBlocks.STONE]?.states?.default ?: throw SkipException("Can not find stone!")
    }

    fun stoneLanding() {
        val player = super.landing()

        player.assertPosition(6.0, 1.0, 6.0)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0)
    }

    fun stoneWalking1() {
        val player = super.walking1()

        player.assertPosition(6.0, 1.0, 6.057036000349253, 2)
        player.assertVelocity(0.0, -0.0784000015258789, 0.034066761351146994, 2)
    }

    fun stoneWalking2() {
        val player = super.walking2()

        player.assertPosition(6.0, 1.0, 7.600801749198814, 10)
        player.assertVelocity(0.0, -0.0784000015258789, 0.11719723621935406, 10)
    }

    fun stoneWalking3() {
        val player = super.walking3()

        player.assertPosition(6.0, 1.0, 16.233706712587654)
        player.assertVelocity(0.0, -0.0784000015258789, 0.1178590650404726)
    }

    fun stoneWalking4() {
        val player = super.walking4()

        player.assertPosition(-6.0, 1.0, -4.399198250801186, 10)
        player.assertVelocity(0.0, -0.0784000015258789, 0.11719723621935406, 10)
    }

    fun stoneWalking5() {
        val player = super.walking5()

        player.assertPosition(-6.0, 1.0, 4.233706712587652)
        player.assertVelocity(0.0, -0.0784000015258789, 0.1178590650404726)
    }

    fun stoneWalking6() {
        val player = super.walking6()

        player.assertPosition(-6.0, 1.0, -7.600801749198814, 10)
        player.assertVelocity(-6.552841365123628E-18, -0.0784000015258789, -0.11719723621935406, 10)
    }

    fun stoneWalking7() {
        val player = super.walking7()

        player.assertPosition(-6.0, 1.0, -16.233706712587654)
        player.assertVelocity(-6.552841365123628E-18, -0.0784000015258789, -0.1178590650404726)
    }

    fun stoneWalking8() {
        val player = super.walking8()

        player.assertPosition(-6.25524367359691, 1.0, -5.700367038791685)
        player.assertVelocity(0.11758154782898447, -0.0784000015258789, 0.0)
    }

    fun stoneWalking9() {
        val player = super.walking9()

        player.assertPosition(-21.100826389493875, -0.14510670065164383, 2.2932725292642133)
        player.assertVelocity(-0.12417366120373599, -0.44749789698341763, -0.07075699355378597)
    }

    fun stoneSprintJump1() {
        val player = super.sprintJump1()

        player.assertPosition(-6.0, 1.0, -5.937083998572499)
        player.assertVelocity(0.0, -0.0784000015258789, 0.03941756248656188)
    }

    fun stoneSprintJump2() {
        val player = super.sprintJump2()

        player.assertPosition(-6.0, 2.0013359791121474, -5.113580188105011)
        player.assertVelocity(0.0, 0.16477328182606651, 0.210140673871018)
    }

    fun stoneSlabWalk() {
        val player = super.slabWalk()

        player.assertPosition(-6.0, 1.5, -5.231489194289152)
        player.assertVelocity(0.0, -0.0784000015258789, 0.0050572549412335206)
        player.assertGround()
    }
}
